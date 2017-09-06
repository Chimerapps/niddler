package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.Nullable;
import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;
import okhttp3.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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
}
