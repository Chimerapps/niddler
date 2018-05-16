package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.Nullable;

import com.icapps.niddler.core.NiddlerRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
final class NiddlerOkHttpRequest implements NiddlerRequest {

	private final Request mRequest;
	private final String mRequestId;
	private final String mMessageId;
	private final long mTimestamp;
	@Nullable
	private final Map<String, String> mExtraHeaders;

	NiddlerOkHttpRequest(final Request request, final String requestId, @Nullable final Map<String, String> extraHeaders) {
		mRequest = request;
		mRequestId = requestId;
		mMessageId = UUID.randomUUID().toString();
		mTimestamp = System.currentTimeMillis();
		mExtraHeaders = extraHeaders;
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
		final Map<String, List<String>> headers = mRequest.headers().toMultimap();
		if (!headers.containsKey("Content-Type") && (mRequest.body() != null) && (mRequest.body().contentType() != null)) {
			headers.put("Content-Type", Collections.singletonList(mRequest.body().contentType().toString()));
		}
		if (mExtraHeaders != null) {
			for (final Map.Entry<String, String> keyValueEntry : mExtraHeaders.entrySet()) {
				if (!headers.containsKey(keyValueEntry.getKey())) {
					headers.put(keyValueEntry.getKey(), Collections.singletonList(keyValueEntry.getValue()));
				}
			}
		}
		return headers;
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
				body.writeTo(buffer);
				buffer.flush();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	static String httpVersion(final Protocol protocol) {
		switch (protocol) {
			case HTTP_1_0:
				return "http/1.0";
			case HTTP_1_1:
				return "http/1.1";
			case SPDY_3:
				return "spdy/3.1";
			case HTTP_2:
				return "http/2.0";
		}
		return "<unknown>";
	}

}
