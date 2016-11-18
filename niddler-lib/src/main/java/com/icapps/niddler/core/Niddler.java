package com.icapps.niddler.core;


import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import com.icapps.niddler.service.NiddlerService;
import com.icapps.niddler.util.Base64;
import com.icapps.niddler.util.StringSizeUtil;
import org.java_websocket.WebSocket;
import trikita.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Maarten Van Giel
 */
public final class Niddler implements NiddlerServer.WebSocketListener, Closeable {

	private final long mMaxCacheSize;
	private final List<String> mMessageCache;
	private final NiddlerServerInfo mNiddlerServerInfo;
	private final ServiceConnection mServiceConnection;
	private NiddlerService mNiddlerService;
	private NiddlerServer mServer;
	private boolean mIsStarted = false;
	private boolean mIsClosed = false;

	private long mCacheSize = 0;

	private Niddler(final int port, final long cacheSize, final NiddlerServerInfo niddlerServerInfo) {
		try {
			mServer = new NiddlerServer(port, this);
		} catch (final UnknownHostException ex) {
			Log.e("Failed to start server: " + ex.getLocalizedMessage());
		}
		mMessageCache = new LinkedList<>();
		mNiddlerServerInfo = niddlerServerInfo;
		mMaxCacheSize = cacheSize;

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mNiddlerService = ((NiddlerService.NiddlerBinder) service).getService();
				mNiddlerService.initialize(Niddler.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mNiddlerService = null;
			}
		};
	}

	public void logRequest(final NiddlerRequest request) {
		final String base64Body = transformBody(request);

		final StringBuilder stringBuilder = new StringBuilder("\"body\":\"");
		stringBuilder.append(base64Body);
		stringBuilder.append("\", \"url\":\"");
		stringBuilder.append(request.getUrl());
		stringBuilder.append("\", \"method\":\"");
		stringBuilder.append(request.getMethod());
		stringBuilder.append("\", \"headers\":{");
		transformHeaders(stringBuilder, request);
		stringBuilder.append("}");

		final String messageJSON = generateMessage(request.getMessageId(), request.getRequestId(), request.getTimestamp(), stringBuilder.toString());
		sendWithCache(messageJSON);
	}

	public void logResponse(final NiddlerResponse response) {
		final String base64Body = transformBody(response);

		final StringBuilder stringBuilder = new StringBuilder("\"statusCode\":");
		stringBuilder.append(response.getStatusCode());
		stringBuilder.append(", \"body\":\"");
		stringBuilder.append(base64Body);
		stringBuilder.append("\", \"headers\":{");
		transformHeaders(stringBuilder, response);
		stringBuilder.append("}");

		final String messageJSON = generateMessage(response.getMessageId(), response.getRequestId(), response.getTimestamp(), stringBuilder.toString());
		sendWithCache(messageJSON);
	}

	private String generateMessage(final String messageId, final String requestId, final long timestamp, final String messageContents) {
		return "{\"requestId\":\"" + requestId + "\", \"messageId\":\"" + messageId + "\", \"timestamp\":" + timestamp + ", " + messageContents + "}";
	}

	private String generateControlMessageContents(final int controlCode, final String data) {
		return "\"controlCode\":" + controlCode + ", \"controlData\":{" + data + "}";
	}

	public void start() {
		if (mServer != null && !mIsStarted) {
			mServer.start();
			mIsStarted = true;
			Log.d("Started listening at address" + mServer.getAddress());
		}
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application the application to attach the Niddler instance to
	 */
	public void attachToApplication(final Application application) {
		application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				// Do nothing
			}

			@Override
			public void onActivityStarted(Activity activity) {
				Intent serviceIntent = new Intent(application, NiddlerService.class);
				application.startService(serviceIntent);
				activity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
			}

			@Override
			public void onActivityResumed(Activity activity) {
				// Do nothing
			}

			@Override
			public void onActivityPaused(Activity activity) {
				// Do nothing
			}

			@Override
			public void onActivityStopped(Activity activity) {
				try {
					activity.unbindService(mServiceConnection);
				} catch (final IllegalArgumentException ignored) {
					//Ignore
				}
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
				// Do nothing
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				// Do nothing
			}
		});
	}

	@Override
	public void close() throws IOException {
		if (mServer != null) {
			try {
				mServer.stop();
				mIsClosed = true;
				if (mNiddlerService != null) {
					mNiddlerService.stopSelf();
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
	}

	public boolean enabled() {
		return true;
	}

	public boolean isStarted() {
		return mIsStarted;
	}

	public boolean isClosed() {
		return mIsClosed;
	}

	private void sendWithCache(final String message) {
		if (mServer != null) {
			if (!mServer.connections().isEmpty()) {
				mServer.sendToAll(message);
				return;
			}

			if (mMaxCacheSize <= 0) {
				return;
			}

			final long messageMemoryUsage = StringSizeUtil.calculateMemoryUsage(message);
			if (mCacheSize + messageMemoryUsage < mMaxCacheSize) {
				mMessageCache.add(message);
				mCacheSize += messageMemoryUsage;
			} else {
				if (messageMemoryUsage > mMaxCacheSize) {
					Log.d("Message too long for cache");
				} else {
					Log.d("Cache is full, removing items until we have enough space");
					while (mCacheSize + messageMemoryUsage >= mMaxCacheSize) {
						final String oldestMessage = mMessageCache.get(0);
						mCacheSize -= StringSizeUtil.calculateMemoryUsage(oldestMessage);
						mMessageCache.remove(oldestMessage);
					}
					mMessageCache.add(message);
					mCacheSize += messageMemoryUsage;
				}
			}
		}
	}

	private String transformBody(final NiddlerMessageBase base) {
		try {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			base.writeBody(os);
			return Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);
		} catch (final IOException e) {
			Log.w("Failed to serialize! " + e.getLocalizedMessage());
			return "";
		}
	}

	private StringBuilder transformHeaders(final StringBuilder builder, final NiddlerMessageBase base) {
		final Map<String, List<String>> headerMap = base.getHeaders();
		final Iterator<String> headerIterator = headerMap.keySet().iterator();

		while (headerIterator.hasNext()) {
			final String headerName = headerIterator.next();
			final List<String> headers = headerMap.get(headerName);

			builder.append("\"");
			builder.append(headerName);
			builder.append("\": [");

			for (String header : headers) {
				builder.append("\"");
				builder.append(header.replace("\"", "\\\"")); // This seems fragile...
				builder.append("\", ");
			}
			if (headers.size() > 0) {
				builder.setLength(builder.length() - 2); // Remove trailing comma
			}

			builder.append("]");

			if (headerIterator.hasNext()) {
				builder.append(", ");
			}
		}
		return builder;
	}

	@Override
	public void onConnectionOpened(WebSocket conn) {
		if (mNiddlerServerInfo != null) {
			final String controlMessage = generateMessage(UUID.randomUUID().toString(), "00000000-0000-0000-0000-000000000000", System.currentTimeMillis(), generateControlMessageContents(1, mNiddlerServerInfo.toJsonString()));
			conn.send(controlMessage);
		}
		for (final String message : mMessageCache) {
			conn.send(message);
		}
	}

	public final static class NiddlerServerInfo {

		private static final int PROTOCOL_VERSION = 1;
		private final String mName;
		private final String mDescription;

		public NiddlerServerInfo(String mName, String mDescription) {
			this.mName = mName;
			this.mDescription = mDescription;
		}

		public String toJsonString() {
			return "\"protocolVersion\":" + PROTOCOL_VERSION + ", \"serverName\":\"" + mName.replace("\"", "\\\"") + "\", \"serverDescription\": \"" + mDescription.replace("\"", "\\\"") + "\"";
		}
	}

	public final static class Builder {

		private int mPort = 6555;
		private long mCacheSize = 1048500; // By default use 1 MB cache
		private NiddlerServerInfo mNiddlerServerInfo = null;

		/**
		 * Sets the port on which Niddler will listen for incoming connections
		 *
		 * @param port The port to be used
		 * @return Builder
		 */
		public Builder setPort(final int port) {
			mPort = port;
			return this;
		}

		/**
		 * Sets the cache size to be used for caching requests and responses while there is no client connected
		 *
		 * @param cacheSize The cache size to be used, in bytes
		 * @return Builder
		 */
		public Builder setCacheSize(final long cacheSize) {
			mCacheSize = cacheSize;
			return this;
		}

		/**
		 * Sets additional information about this Niddler server which will be shown on the client side
		 *
		 * @param niddlerServerInfo The additional information about this Niddler server
		 * @return Builder
		 */
		public Builder setNiddlerInformation(final NiddlerServerInfo niddlerServerInfo) {
			mNiddlerServerInfo = niddlerServerInfo;
			return this;
		}

		/**
		 * Sets the NiddlerServerInformation to the application's package name and device info
		 *
		 * @param application the current application
		 * @return Builder
		 */
		public Builder forApplication(final Application application) {
			mNiddlerServerInfo = new NiddlerServerInfo(application.getPackageName(), android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT);
			return this;
		}

		/**
		 * Builds a Niddler instance with the configured parameters
		 *
		 * @return a Niddler instance
		 */
		public Niddler build() {
			return new Niddler(mPort, mCacheSize, mNiddlerServerInfo);
		}

	}

}
