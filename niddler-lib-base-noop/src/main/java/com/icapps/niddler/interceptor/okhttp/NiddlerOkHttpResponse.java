package com.icapps.niddler.interceptor.okhttp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * @author Maarten Van Giel
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerOkHttpResponse implements NiddlerResponse {

	public NiddlerOkHttpResponse(final Response response, final String requestId) {
		// Dummy implementation
	}

	@Override
	public String getMessageId() {
		return null;
	}

	@Override
	public String getRequestId() {
		return null;
	}

	@Override
	public long getTimestamp() {
		return 0;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return null;
	}

	@Override
	public Integer getStatusCode() {
		return null;
	}

	@Override
	public void writeBody(final OutputStream stream) throws IOException {
		// Do nothing
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
		return "";
	}

	@NonNull
	@Override
	public String getHttpVersion() {
		return "";
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

	@Nullable
	@Override
	public StackTraceElement[] getErrorStackTrace() {
		return null;
	}
}
