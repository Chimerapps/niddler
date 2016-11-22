package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.NiddlerRequest;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Maarten Van Giel
 */
final class NiddlerOkHttpRequest implements NiddlerRequest {

	private final Request mRequest;
	private final String mRequestId;
	private final String mMessageId;
	private final long mTimestamp;

	NiddlerOkHttpRequest(final Request request, final String requestId) {
		mRequest = request;
		mRequestId = requestId;
		mMessageId = UUID.randomUUID().toString();
		mTimestamp = System.currentTimeMillis();
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
	public String getUrl() {
		return mRequest.url().toString();
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return mRequest.headers().toMultimap();
	}

	@Override
	public String getMethod() {
		return mRequest.method();
	}

	@Override
	public void writeBody(final OutputStream stream) {
		try {
			final BufferedSink buffer = Okio.buffer(Okio.sink(stream));

			final RequestBody body = mRequest.body();
			if (body != null) {
				mRequest.body().writeTo(buffer);
				buffer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}