package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.NiddlerRequest;
import okhttp3.Request;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpRequest implements NiddlerRequest {

	public NiddlerOkHttpRequest(Request request, String requestId) {
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
	public void writeBody(OutputStream stream) {
		// Do nothing
	}

}
