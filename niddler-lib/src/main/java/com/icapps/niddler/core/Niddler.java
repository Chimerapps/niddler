package com.icapps.niddler.core;


import com.icapps.niddler.util.Base64;
import com.icapps.niddler.util.StringSizeUtil;
import org.java_websocket.WebSocket;
import trikita.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Maarten Van Giel
 */
public final class Niddler implements NiddlerServer.WebSocketListener {

	private final long mMaxCacheSize;
	private final List<String> mMessageCache;
	private final NiddlerServer mServer;

	private long mCacheSize = 0;
	private boolean mEnabled;

	private Niddler(final int port, final long cacheSize) throws UnknownHostException {
		mServer = new NiddlerServer(port, this);
		mMessageCache = new LinkedList<>();
		mMaxCacheSize = cacheSize;
	}

	public void logRequest(final NiddlerRequest request) {
		final String base64Body = transformBody(request);

		final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
		stringBuilder.append(request.getRequestId());
		stringBuilder.append("\", \"messageId\":\"");
		stringBuilder.append(request.getMessageId());
		stringBuilder.append("\", \"url\":\"");
		stringBuilder.append(request.getUrl());
		stringBuilder.append("\", \"timestamp\":");
		stringBuilder.append(request.getTimestamp());
		stringBuilder.append(", \"method\":\"");
		stringBuilder.append(request.getMethod());
		stringBuilder.append("\", \"body\":\"");
		stringBuilder.append(base64Body);
		stringBuilder.append("\", \"headers\": {");

		transformHeaders(stringBuilder, request);

		stringBuilder.append("}}");
		sendWithCache(stringBuilder.toString());
	}

	public void logResponse(final NiddlerResponse response) {
		final String base64Body = transformBody(response);

		final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
		stringBuilder.append(response.getRequestId());
		stringBuilder.append("\", \"messageId\":\"");
		stringBuilder.append(response.getMessageId());
		stringBuilder.append("\", \"timestamp\":");
		stringBuilder.append(response.getTimestamp());
		stringBuilder.append(", \"statusCode\":");
		stringBuilder.append(response.getStatusCode());
		stringBuilder.append(", \"body\":\"");
		stringBuilder.append(base64Body);
		stringBuilder.append("\", \"headers\": {");

		transformHeaders(stringBuilder, response);

		stringBuilder.append("}}");
		sendWithCache(stringBuilder.toString());
	}

	public void start() {
		mServer.start();
		Log.d("Started listening at address" + mServer.getAddress());
	}

	public void close() throws IOException, InterruptedException {
		mServer.stop();
	}

	public boolean enabled() {
		return true;
	}

	private void sendWithCache(final String message) {
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
		for (final String message : mMessageCache) {
			conn.send(message);
		}
	}

	public final static class Builder {

		private int mPort = 6555;
		private long mCacheSize = 1048500; // By default use 1 MB cache

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
		 * Builds a Niddler instance with the configured parameters
		 *
		 * @return a Niddler instance
		 * @throws UnknownHostException
		 */
		public Niddler build() throws UnknownHostException {
			return new Niddler(mPort, mCacheSize);
		}

	}

}
