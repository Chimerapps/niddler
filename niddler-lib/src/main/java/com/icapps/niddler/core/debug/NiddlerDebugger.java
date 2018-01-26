package com.icapps.niddler.core.debug;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Nicola Verbeeck
 * @version 1
 */

public interface NiddlerDebugger {

	boolean isActive();

	boolean isBlacklisted(@NonNull final CharSequence url);

	@Nullable
	DebugResponse handleRequest(@NonNull final NiddlerRequest request);

	@Nullable
	DebugResponse handleResponse(@NonNull final NiddlerRequest request, @NonNull final NiddlerResponse response) throws ExecutionException, InterruptedException;

	class DebugResponse {
		public final int code;

		@NonNull
		public final String message;

		@Nullable
		public final Map<String, String> headers;

		@Nullable
		public final String encodedBody;

		@Nullable
		public final String bodyMimeType;

		public DebugResponse(final int code,
				@NonNull final String message,
				@Nullable final Map<String, String> headers,
				@Nullable final String encodedBody,
				@Nullable final String bodyMimeType) {
			this.code = code;
			this.message = message;
			this.headers = headers;
			this.encodedBody = encodedBody;
			this.bodyMimeType = bodyMimeType;
		}
	}

}
