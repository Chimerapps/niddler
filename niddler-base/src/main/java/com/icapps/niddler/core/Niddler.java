package com.icapps.niddler.core;

import android.support.annotation.NonNull;

import com.icapps.niddler.core.debug.NiddlerDebugger;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
@SuppressWarnings("WeakerAccess")
public class Niddler implements Closeable {

	public static final String NIDDLER_DEBUG_RESPONSE_HEADER = "X-Niddler-Debug";
	public static final String NIDDLER_DEBUG_TIMING_RESPONSE_HEADER = "X-Niddler-Debug-Timing";
	public static final String INTENT_EXTRA_WAIT_FOR_DEBUGGER = "Niddler-Wait-For-Debugger";

	final NiddlerImpl mNiddlerImpl;

	protected Niddler(final String password, final int port, final long cacheSize, final NiddlerServerInfo niddlerServerInfo) {
		mNiddlerImpl = new NiddlerImpl(password, port, cacheSize, niddlerServerInfo);
	}

	public void logRequest(final NiddlerRequest request) {
		mNiddlerImpl.send(MessageBuilder.buildMessage(request));
	}

	public void logResponse(final NiddlerResponse response) {
		mNiddlerImpl.send(MessageBuilder.buildMessage(response));
	}

	public void start() {
		mNiddlerImpl.start();
	}

	/**
	 * Niddler supports a single active debugger at any given time. This debugger has capabilities that add items to a blacklist, return default responses upon request, ...
	 * Interceptors should implement as many of the integrations as they can. For an example take a look at {@link com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor}
	 *
	 * @return The debugger for niddler
	 */
	@NonNull
	public NiddlerDebugger debugger() {
		return mNiddlerImpl.debugger();
	}

	@Override
	public void close() throws IOException {
		mNiddlerImpl.close();
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
		return mNiddlerImpl.isStarted();
	}

	/**
	 * @return True if the server is stopped
	 */
	public boolean isClosed() {
		return mNiddlerImpl.isClosed();
	}

	/**
	 * @return The socket port we are listening on
	 */
	public int getPort() {
		return mNiddlerImpl.getPort();
	}

	@SuppressWarnings({"WeakerAccess", "unused", "PackageVisibleField", "StaticMethodOnlyUsedInOneClass"})
	public static final class NiddlerServerInfo {

		/**
		 * Protocol version 4:
		 * - Support for configuration (blacklist, basic debugging)
		 */
		static final int PROTOCOL_VERSION = 4;
		final String name;
		final String description;

		public NiddlerServerInfo(final String name, final String description) {
			this.name = name;
			this.description = description;
		}

	}

	@SuppressWarnings({"unused", "SameParameterValue", "MagicNumber"})
	public static abstract class Builder<T extends Niddler> {

		protected int mPort = 6555;
		protected long mCacheSize = 1024 * 1024; // By default use 1 MB cache
		protected NiddlerServerInfo mNiddlerServerInfo = null;
		protected String mPassword;

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
		public Builder<T> setPort(final int port) {
			mPort = port;
			return this;
		}

		/**
		 * Sets the cache size to be used for caching requests and responses while there is no client connected
		 *
		 * @param cacheSize The cache size to be used, in bytes
		 * @return Builder
		 */
		public Builder<T> setCacheSize(final long cacheSize) {
			mCacheSize = cacheSize;
			return this;
		}

		/**
		 * Sets additional information about this Niddler server which will be shown on the client side
		 *
		 * @param niddlerServerInfo The additional information about this Niddler server
		 * @return Builder
		 */
		public Builder<T> setNiddlerInformation(final NiddlerServerInfo niddlerServerInfo) {
			mNiddlerServerInfo = niddlerServerInfo;
			return this;
		}

		/**
		 * Builds a Niddler instance with the configured parameters
		 *
		 * @return a Niddler instance
		 */
		public abstract T build();

	}

	interface PlatformNiddler {
		void close();
	}
}
