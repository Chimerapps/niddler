package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.Nullable;
import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Maarten Van Giel
 */
final class NiddlerOkHttpResponse implements NiddlerResponse {

	private final Response mResponse;
	private final String mRequestId;
	private final String mMessageId;
	private final long mTimestamp;
	@Nullable
	private final NiddlerRequest mActualNetworkRequest;
	@Nullable
	private final NiddlerResponse mActualNetworkReply;

	NiddlerOkHttpResponse(final Response response,
	                      final String requestId,
	                      @Nullable final NiddlerRequest actualNetworkRequest,
	                      @Nullable final NiddlerResponse actualNetworkReply) {
		this.mResponse = response;
		this.mRequestId = requestId;
		mActualNetworkRequest = actualNetworkRequest;
		mActualNetworkReply = actualNetworkReply;
		this.mMessageId = UUID.randomUUID().toString();
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
	public Map<String, List<String>> getHeaders() {
		return mResponse.headers().toMultimap();
	}

	@Override
	public Integer getStatusCode() {
		return mResponse.code();
	}

	@Nullable
	@Override
	public NiddlerRequest actualNetworkRequest() {
		return mActualNetworkRequest;
	}

	@Nullable
	@Override
	public NiddlerResponse actualNetworkReply() {
		return mActualNetworkReply;
	}

	@Override
	public void writeBody(final OutputStream stream) {
		final ResponseBody body = mResponse.body();
		try {
			if (body != null) {
				final BufferedSource source = body.source();
				source.request(Long.MAX_VALUE); // Buffer entire body

				final Buffer buffer = source.buffer();
				stream.write(buffer.clone().readByteArray());
				stream.flush();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
