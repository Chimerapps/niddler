package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 */
final class NiddlerOkHttpErrorResponse implements NiddlerResponse {

	private final String mRequestId;
	private final String mMessageId;
	private final long mTimestamp;
	private final String mStatusLine;
	private final StackTraceElement[] mStackTrace;

	NiddlerOkHttpErrorResponse(@NonNull final String requestId, @NonNull final Throwable error) {
		mRequestId = requestId;
		mMessageId = UUID.randomUUID().toString();
		mTimestamp = System.currentTimeMillis();

		mStackTrace = error.getStackTrace().clone();

		if (error instanceof SocketTimeoutException) {
			mStatusLine = "TIMEOUT";
		} else {
			mStatusLine = error.getMessage();
		}
	}

	@Override
	public String getMessageId() {
		return mMessageId;
	}

	@Override
	public String getRequestId() {
		return mRequestId;
	}

	@Override
	public long getTimestamp() {
		return mTimestamp;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return Collections.emptyMap();
	}

	@Override
	public Integer getStatusCode() {
		return 0;
	}

	@Nullable
	@Override
	public NiddlerRequest actualNetworkRequest() {
		return null;
	}

	@Nullable
	@Override
	public NiddlerResponse actualNetworkReply() {
		return null;
	}

	@NonNull
	@Override
	public String getStatusLine() {
		return mStatusLine;
	}

	@NonNull
	@Override
	public String getHttpVersion() {
		return "<unknown>";
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
		return -1;
	}

	@Override
	public void writeBody(final OutputStream stream) {
	}

	@Nullable
	@Override
	public StackTraceElement[] getErrorStackTrace() {
		return mStackTrace;
	}
}
