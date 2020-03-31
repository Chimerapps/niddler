package com.icapps.niddler.interceptor.okhttp;

import androidx.annotation.Nullable;

import com.icapps.niddler.core.NiddlerRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * @author Maarten Van Giel
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerOkHttpRequest implements NiddlerRequest {

	public NiddlerOkHttpRequest(final Request request, final String requestId) {
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
	public String getUrl() {
		return null;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return null;
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public void writeBody(final OutputStream stream) throws IOException {
		// Do nothing
	}

	@Nullable
	@Override
	public StackTraceElement[] getRequestStackTrace() {
		return null;
	}

	@Nullable
	@Override
	public List<String> getRequestContext() {
		return null;
	}
}
