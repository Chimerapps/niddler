package com.icapps.niddler.core;


import java.io.IOException;
import java.net.UnknownHostException;

/**
 * @author Maarten Van Giel
 */

@SuppressWarnings({"UnusedParameters", "unused"})
public final class Niddler {

	private Niddler(final int port, final long cacheSize) throws UnknownHostException {
		// Dummy implementation
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

	public void close() throws IOException, InterruptedException {
		// Do nothing
	}

	public boolean enabled() {
		return false;
	}

	public final static class Builder {

		public Builder setPort(final int port) {
			return this;
		}

		public Builder setCacheSize(final long cacheSize) {
			return this;
		}

		public Niddler build() throws UnknownHostException {
			return new Niddler(0, 0);
		}

	}

}
