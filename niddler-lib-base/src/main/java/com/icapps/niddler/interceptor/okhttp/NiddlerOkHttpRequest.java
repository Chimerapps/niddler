package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.NiddlerRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.MediaType;
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
	@Nullable
	private final StackTraceElement[] mStackTraceElements;
	@Nullable
	private final NiddlerOkHttpInterceptor.NiddlerRequestContext mRequestContext;
	private final Map<String, String> mMetadata;

	NiddlerOkHttpRequest(final Request request, final String requestId, @Nullable final Map<String, String> extraHeaders,
			@Nullable final StackTraceElement[] stackTraceElements,
			@Nullable final NiddlerOkHttpInterceptor.NiddlerRequestContext requestContext,
			@Nullable final Map<String, String> metadata) {
		mRequest = request;
		mRequestId = requestId;
		mMessageId = UUID.randomUUID().toString();
		mTimestamp = System.currentTimeMillis();
		mExtraHeaders = extraHeaders;
		mStackTraceElements = stackTraceElements;
		mRequestContext = requestContext;
		mMetadata = new TreeMap<>();

		if (metadata != null) {
			mMetadata.putAll(metadata);
		}
	}

	public void addMetadata(@NonNull final String key, @NonNull final String value) {
		mMetadata.put(key, value);
	}

	@NonNull
	@Override
	public String getMessageId() {
		return mMessageId;
	}

	@NonNull
	@Override
	public String getRequestId() {
		return mRequestId;
	}

	@Override
	public long getTimestamp() {
		return mTimestamp;
	}

	@NonNull
	@Override
	public String getUrl() {
		return mRequest.url().toString();
	}

	@NonNull
	@Override
	public Map<String, String> getMetadata() {
		return mMetadata;
	}

	@NonNull
	@Override
	public Map<String, List<String>> getHeaders() {
		final Map<String, List<String>> headers = mRequest.headers().toMultimap();
		if (!headers.containsKey("Content-Type") && (mRequest.body() != null)) {
			final MediaType contentType = mRequest.body().contentType();
			if (contentType != null) {
				headers.put("Content-Type", Collections.singletonList(contentType.toString()));
			}
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

	@Nullable
	@Override
	public StackTraceElement[] getRequestStackTrace() {
		return mStackTraceElements;
	}

	@NonNull
	@Override
	public String getMethod() {
		return mRequest.method();
	}

	@Nullable
	@Override
	public List<String> getRequestContext() {
		if (mRequestContext == null) {
			return null;
		}
		return mRequestContext.getContextInformation();
	}

	@Override
	public void writeBody(@NonNull final OutputStream stream) {
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
