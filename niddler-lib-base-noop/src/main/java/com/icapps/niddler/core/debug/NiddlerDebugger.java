package com.icapps.niddler.core.debug;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public interface NiddlerDebugger {

	/**
	 * Checks if the debugger is active at the time
	 *
	 * @return True if the debugger was active when checking, false if it is disabled
	 */
	boolean isActive();

	/**
	 * Checks if the given url has been added to the blacklist and thus does not need to be tracked
	 *
	 * @param url The url to check
	 * @return True if the request should not be logged through niddler, false if there is no blacklist for the url
	 */
	boolean isBlacklisted(@NonNull final CharSequence url);

	/**
	 * Override the request with an updated version. This is executed AFTER checking the blacklist and when the request is overridden, it is NOT checked again against the blacklist
	 * If the request is overridden, it will still follow the regular flow of being checked against other debugger actions (handleRequest, ...)
	 *
	 * @param request The request to match
	 * @return The new request to use instead of the original request or null when the request has not been overridden
	 */
	@Nullable
	DebugRequest overrideRequest(@NonNull final NiddlerRequest request);

	/**
	 * Checks if the debugger wants to override the response for the given request. This is executed BEFORE an actual request is put on the network
	 *
	 * @param request The request to match
	 * @return Either the response to use instead of performing the network call or null when the debugger does not want to interfere at this time
	 */
	@Nullable
	DebugResponse handleRequest(@NonNull final NiddlerRequest request);

	/**
	 * Checks if the debugger wants to override the response for the given request. This is executed AFTER performing the actual network request and thus allows the debugger
	 * to override what is reported back to the client
	 *
	 * @param request  The request to match
	 * @param response The response to match
	 * @return Either the response to use instead the network response or null when the debugger does not want to interfere at this time
	 */
	@Nullable
	DebugResponse handleResponse(@NonNull final NiddlerRequest request, @NonNull final NiddlerResponse response);

	/**
	 * Applies a delay before executing the request. This delay is executed BEFORE checking if the request should have been blacklisted.
	 * See {@link com.icapps.niddler.core.Niddler#NIDDLER_DEBUG_TIMING_RESPONSE_HEADER}
	 *
	 * @return True if the system interfered by causing a delay, false if no delay was added.
	 * @throws IOException When sleeping the thread fails
	 */
	boolean applyDelayBeforeBlacklist() throws IOException;

	/**
	 * Applies a delay before executing the request. This delay is executed after checking if the request should have been blacklisted, this means that blacklisted items will
	 * not be delayed.
	 * See {@link com.icapps.niddler.core.Niddler#NIDDLER_DEBUG_TIMING_RESPONSE_HEADER}
	 *
	 * @return True if the system interfered by causing a delay, false if no delay was added.
	 * @throws IOException When sleeping the thread fails
	 */
	boolean applyDelayAfterBlacklist() throws IOException;

	/**
	 * Ensures that the request took at least a predetermined time (in milliseconds) to execute.
	 * This provides more granular control over the execution time as this takes into account the time the request has spent waiting for network and for the debugger
	 * See {@link com.icapps.niddler.core.Niddler#NIDDLER_DEBUG_TIMING_RESPONSE_HEADER}
	 *
	 * @param startTime The system time in nanoseconds ({@link System#nanoTime()}) right before the request
	 * @return True if the system interfered by causing a delay, false if no delay was added.
	 * @throws IOException When sleeping the thread fails
	 */
	boolean ensureCallTime(final long startTime) throws IOException;

	/**
	 * Instructs the debugger server to hold all calls until a debugger has connected and has started a session. Can be cancelled with {#cancelWaitForConnection}
	 *
	 * @param onDebuggerConnected Callback to be executed when a debugger has connected and started a session. Can be called on any thread
	 * @return True if we transitioned to the waiting state and the called should inform the user, false if not
	 */
	boolean waitForConnection(@NonNull final Runnable onDebuggerConnected);

	/**
	 * @return True if the debugger server is currently waiting until a debugger has connected
	 */
	boolean isWaitingForConnection();

	/**
	 * Tells the debugger server to stop waiting for a debugger connection and proceed
	 */
	void cancelWaitForConnection();

	abstract class DebugMessage {
		/**
		 * Headers, optional
		 */
		@Nullable
		public final Map<String, List<String>> headers;

		/**
		 * base64 encoded body if any
		 */
		@Nullable
		public final String encodedBody;

		/**
		 * Mime type of the body, must be set if the body is non-null
		 */
		@Nullable
		public final String bodyMimeType;

		DebugMessage(@Nullable final Map<String, List<String>> headers, @Nullable final String encodedBody, @Nullable final String bodyMimeType) {
			this.headers = headers;
			this.encodedBody = encodedBody;
			this.bodyMimeType = bodyMimeType;
		}
	}

	class DebugResponse extends DebugMessage {
		/**
		 * Status code
		 */
		public final int code;

		/**
		 * Status message
		 */
		@NonNull
		public final String message;

		public DebugResponse(final int code,
				@NonNull final String message,
				@Nullable final Map<String, List<String>> headers,
				@Nullable final String encodedBody,
				@Nullable final String bodyMimeType) {
			super(headers, encodedBody, bodyMimeType);
			this.code = code;
			this.message = message;
		}
	}

	class DebugRequest extends DebugMessage {
		/**
		 * The url of the request, required
		 */
		@NonNull
		public final String url;

		/**
		 * Method of the request, required
		 */
		@NonNull
		public final String method;

		public DebugRequest(@NonNull final String url,
				@NonNull final String method,
				@Nullable final Map<String, List<String>> headers,
				@Nullable final String encodedBody,
				@Nullable final String bodyMimeType) {
			super(headers, encodedBody, bodyMimeType);
			this.url = url;
			this.method = method;
		}
	}

}
