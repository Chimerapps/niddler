package com.icapps.niddler.core;


import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.icapps.niddler.service.NiddlerService;
import com.icapps.niddler.util.Logging;

import org.java_websocket.WebSocket;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 *         TODO: 22/11/16 - Hide the interface we implement, this pollutes the public api
 */
@SuppressWarnings("WeakerAccess")
public final class Niddler implements NiddlerServer.WebSocketListener, Closeable {

	private static final String LOG_TAG = Niddler.class.getSimpleName();

	private final NiddlerServerInfo mNiddlerServerInfo;
	private final MessagesCache mMessageCache;
	private final NiddlerServiceLifeCycleWatcher mLifeCycleWatcher;
	private NiddlerService mNiddlerService;
	private NiddlerServer mServer;
	private boolean mIsStarted = false;
	private boolean mIsClosed = false;
	private long mAutoStopAfter = -1;

	private Niddler(final String password, final int port, final long cacheSize, final NiddlerServerInfo niddlerServerInfo) {
		try {
			mServer = new NiddlerServer(password, port, niddlerServerInfo.name, this);
		} catch (final UnknownHostException ex) {
			Log.e(LOG_TAG, "Failed to start server: " + ex.getLocalizedMessage());
		}
		mMessageCache = new MessagesCache(cacheSize);
		mNiddlerServerInfo = niddlerServerInfo;

		mLifeCycleWatcher = new NiddlerServiceLifeCycleWatcher(new ServiceConnection() {
			@Override
			public void onServiceConnected(final ComponentName name, final IBinder service) {
				mNiddlerService = ((NiddlerService.NiddlerBinder) service).getService();
				mNiddlerService.initialize(Niddler.this, mAutoStopAfter);
			}

			@Override
			public void onServiceDisconnected(final ComponentName name) {
				mNiddlerService = null;
			}
		});
	}

	public void logRequest(final NiddlerRequest request) {
		sendWithCache(MessageBuilder.buildMessage(request));
	}

	public void logResponse(final NiddlerResponse response) {
		sendWithCache(MessageBuilder.buildMessage(response));
	}

	public void start() {
		if ((mServer != null) && !mIsStarted) {
			mServer.start();
			mIsStarted = true;
			if (Logging.DO_LOG) {
				Log.d(LOG_TAG, "Started listening at address" + mServer.getAddress());
			}
		}
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application the application to attach the Niddler instance to
	 */
	public void attachToApplication(final Application application) {
		attachToApplication(application, -1L);
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application   the application to attach the Niddler instance to
	 * @param autoStopAfter Automatically stop the niddler background service after x milliseconds. Use -1 to keep the service running and use 0 to stop the service immediately
	 */
	public void attachToApplication(final Application application, final long autoStopAfter) {
		mAutoStopAfter = autoStopAfter;
		application.unregisterActivityLifecycleCallbacks(mLifeCycleWatcher);
		application.registerActivityLifecycleCallbacks(mLifeCycleWatcher);
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
			} catch (final InterruptedException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void onConnectionOpened(final WebSocket conn) {
		if (mNiddlerServerInfo != null) {
			conn.send(MessageBuilder.buildMessage(mNiddlerServerInfo));
		}
		for (final String message : mMessageCache.get()) {
			conn.send(message);
		}
	}

	/**
	 * Indicates if niddler is configured to log requests, use this to determine in your interceptor if you need
	 * to generate a message
	 *
	 * @return True if this is the real niddler, in no-op mode, this returns false
	 */
	@SuppressWarnings("unused")
	public static boolean enabled() {
		return true;
	}

	/**
	 * @return True if the niddler server is started
	 */
	public boolean isStarted() {
		return mIsStarted;
	}

	/**
	 * @return True if the server is stopped
	 */
	public boolean isClosed() {
		return mIsClosed;
	}

	private void sendWithCache(final String message) {
		if (mServer != null) {
			mMessageCache.put(message);
			mServer.sendToAll(message);
		}
	}

	/**
	 * @return The socket port we are listening on
	 */
	public int getPort() {
		return mServer.getPort();
	}

	@SuppressWarnings({"WeakerAccess", "unused", "PackageVisibleField", "StaticMethodOnlyUsedInOneClass"})
	public static final class NiddlerServerInfo {

		static final int PROTOCOL_VERSION = 3;
		final String name;
		final String description;

		public NiddlerServerInfo(final String name, final String description) {
			this.name = name;
			this.description = description;
		}

		/**
		 * Creates a server info based on the application's package name and some device fields
		 *
		 * @param application The application niddler is instrumenting
		 * @return A server info document to use in the {@link Builder}
		 */
		public static NiddlerServerInfo fromApplication(final Application application) {
			return new NiddlerServerInfo(application.getPackageName(), Build.MANUFACTURER + " " + Build.PRODUCT);
		}
	}

	@SuppressWarnings({"unused", "SameParameterValue", "MagicNumber"})
	public static final class Builder {

		private int mPort = 6555;
		private long mCacheSize = 1024 * 1024; // By default use 1 MB cache
		private NiddlerServerInfo mNiddlerServerInfo = null;
		private String mPassword;

		/**
		 * Creates a new builder with a given password to use for the niddler server authentication
		 *
		 * @param password The password to use
		 */
		public Builder(final String password) {
			mPassword = password;
		}

		/**
		 * Creates a new builder that has authentication disabled
		 */
		public Builder() {
		}

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
		 * Builds a Niddler instance with the configured parameters
		 *
		 * @return a Niddler instance
		 */
		public Niddler build() {
			return new Niddler(mPassword, mPort, mCacheSize, mNiddlerServerInfo);
		}

	}

}
