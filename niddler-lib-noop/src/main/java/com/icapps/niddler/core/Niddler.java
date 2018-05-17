package com.icapps.niddler.core;


import android.app.Application;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
@SuppressWarnings({"UnusedParameters", "unused"})
public abstract class Niddler implements Closeable {

	Niddler() {
	}

	public void logRequest(final NiddlerRequest request) {
		// Do nothing
	}

	public void logResponse(final NiddlerResponse response) {
		// Do nothing
	}

	public void start() {
		// Do nothing
	}

	public void attachToApplication(final Application application) {
		// Do nothing
	}

	public void attachToApplication(final Application application, final long timeOut) {
		// Do nothing
	}

	@Override
	public void close() throws IOException {
		// Do nothing
	}

	public int getPort() {
		return -1;
	}

	public static boolean enabled() {
		return false;
	}

	@SuppressWarnings("MethodMayBeStatic")
	public boolean isStarted() {
		return false;
	}

	@SuppressWarnings("MethodMayBeStatic")
	public boolean isClosed() {
		return false;
	}

	@SuppressWarnings("WeakerAccess")
	public static final class NiddlerServerInfo {

		public NiddlerServerInfo(final String name, final String description) {
			// Do nothing
		}

		/**
		 * Creates a server info based on the application's package name and some device fields
		 *
		 * @param application The application niddler is instrumenting
		 * @return A server info document to use in the {@link Builder}
		 */
		public static NiddlerServerInfo fromApplication(final Application application) {
			return new NiddlerServerInfo("", "");
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static abstract class Builder<T extends Niddler> {

		public Builder(final String a) {
		}

		public Builder() {
		}

		/**
		 * Sets the port on which Niddler will listen for incoming connections
		 *
		 * @param port The port to be used
		 * @return Builder
		 */
		public Builder<T> setPort(final int port) {
			return this;
		}

		/**
		 * Sets the cache size to be used for caching requests and responses while there is no client connected
		 *
		 * @param cacheSize The cache size to be used, in bytes
		 * @return Builder
		 */
		public Builder<T> setCacheSize(final long cacheSize) {
			return this;
		}

		/**
		 * Sets additional information about this Niddler server which will be shown on the client side
		 *
		 * @param niddlerServerInfo The additional information about this Niddler server
		 * @return Builder
		 */
		public Builder<T> setNiddlerInformation(final NiddlerServerInfo niddlerServerInfo) {
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
		void closePlatform();
	}

}
