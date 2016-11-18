package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.NiddlerResponse;
import okhttp3.Response;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpResponse implements NiddlerResponse {

	public NiddlerOkHttpResponse(Response response, String requestId) {
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
	public void writeBody(OutputStream stream) {
		// Do nothing
	}

}
