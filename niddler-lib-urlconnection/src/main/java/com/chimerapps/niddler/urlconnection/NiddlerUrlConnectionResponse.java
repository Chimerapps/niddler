package com.chimerapps.niddler.urlconnection;

import com.chimerapps.niddler.core.NiddlerRequest;
import com.chimerapps.niddler.core.NiddlerResponse;

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
class NiddlerUrlConnectionResponse implements NiddlerResponse {

	private final String messageId = UUID.randomUUID().toString();

	@NonNull
	final String requestId;
	@NonNull
	private final Map<String, List<String>> headers;
	@NonNull
	private final Map<String, String> metadata;
	@Nullable
	private final byte[] body;
	private final int statusCode;
	private final String statusLine;
	private final String httpVersion;
	private final int waitTime;

	NiddlerUrlConnectionResponse(@NonNull final String requestId,
			@NonNull final Map<String, List<String>> headers,
			@NonNull final Map<String, String> metadata,
			@Nullable final byte[] body,
			final int statusCode,
			final String statusLine,
			final String httpVersion,
			final int waitTime) {
		this.requestId = requestId;
		this.headers = headers;
		this.metadata = metadata;
		this.body = body;
		this.statusCode = statusCode;
		this.statusLine = statusLine;
		this.httpVersion = httpVersion;
		this.waitTime = waitTime;
	}

	@NonNull
	@Override
	public Integer getStatusCode() {
		return statusCode;
	}

	@NonNull
	@Override
	public String getStatusLine() {
		return statusLine;
	}

	@NonNull
	@Override
	public String getHttpVersion() {
		return httpVersion;
	}

	@Nullable
	@Override
	public NiddlerRequest actualNetworkRequest() {
		return null; //TODO
	}

	@Nullable
	@Override
	public NiddlerResponse actualNetworkReply() {
		return null; //TODO
	}

	@Nullable
	@Override
	public StackTraceElement[] getErrorStackTrace() {
		return null; //TODO
	}

	@Override
	public int getWriteTime() {
		return -1;
	}

	@Override
	public int getReadTime() {
		return -1;
	}

	@Override
	public int getWaitTime() {
		return waitTime;
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
