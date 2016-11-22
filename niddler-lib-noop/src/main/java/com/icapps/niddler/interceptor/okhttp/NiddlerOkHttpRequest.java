package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.NiddlerRequest;
import okhttp3.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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

}
