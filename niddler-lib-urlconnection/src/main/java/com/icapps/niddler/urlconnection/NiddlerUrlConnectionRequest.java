package com.icapps.niddler.urlconnection;

import com.icapps.niddler.core.NiddlerRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 */
class NiddlerUrlConnectionRequest implements NiddlerRequest {

	private final String messageId = UUID.randomUUID().toString();

	@NonNull
	final String requestId;
	@NonNull
	private final String url;
	@NonNull
	private final String method;
	@NonNull
	private final Map<String, List<String>> headers;
	@NonNull
	private final Map<String, String> metadata;
	@Nullable
	private final byte[] body;
	@Nullable
	private final StackTraceElement[] requestStackTrace;
	@Nullable
	private final List<String> requestContext;

	NiddlerUrlConnectionRequest(@NonNull final String requestId,
			@NonNull final String url,
			@NonNull final String method,
			@NonNull final Map<String, List<String>> headers,
			@NonNull final Map<String, String> metadata,
			@Nullable final byte[] body,
			@Nullable final StackTraceElement[] requestStackTrace,
			@Nullable final List<String> requestContext) {
		this.requestId = requestId;
		this.url = url;
		this.method = method;
		this.headers = headers;
		this.metadata = metadata;
		this.body = body;
		this.requestStackTrace = requestStackTrace;
		this.requestContext = requestContext;
	}

	@NonNull
	@Override
	public String getUrl() {
		return url;
	}

	@NonNull
	@Override
	public String getMethod() {
		return method;
	}

	@Nullable
	@Override
	public StackTraceElement[] getRequestStackTrace() {
		return requestStackTrace;
	}

	@Nullable
	@Override
	public List<String> getRequestContext() {
		return requestContext;
	}

	@NonNull
	@Override
	public String getMessageId() {
		return messageId;
	}

	@NonNull
	@Override
	public String getRequestId() {
		return requestId;
	}

	@Override
	public long getTimestamp() {
		return System.currentTimeMillis();
	}

	@NonNull
	@Override
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	@NonNull
	@Override
	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public void writeBody(@NonNull final OutputStream stream) throws IOException {
		if (body != null) {
			stream.write(body);
		}
	}
}
