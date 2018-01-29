package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;
import com.icapps.niddler.core.debug.NiddlerDebugger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.icapps.niddler.core.Niddler.NIDDLER_DEBUG_RESPONSE_HEADER;

/**
 * @author Nicola Verbeeck
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

	private final Niddler mNiddler;
	private final List<Pattern> mBlacklist;
	@NonNull
	private final NiddlerDebugger mDebugger;

	public NiddlerOkHttpInterceptor(final Niddler niddler) {
		mNiddler = niddler;
		mBlacklist = new ArrayList<>();
		mDebugger = niddler.debugger();
	}

	/**
	 * Adds a static blacklist on the given url pattern. The pattern is interpreted as a java regex ({@link Pattern}). Items matching the blacklist are not tracked by niddler.
	 * This blacklist is independent from any debugger blacklists
	 *
	 * @param urlPattern The pattern to add to the blacklist
	 * @return This instance
	 */
	@SuppressWarnings("unused")
	public NiddlerOkHttpInterceptor blacklist(@NonNull final String urlPattern) {
		mBlacklist.add(Pattern.compile(urlPattern));
		return this;
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		final Request request = chain.request();
		mDebugger.applyDelayBeforeBlacklist();
		if (isBlacklisted(request.url().toString())) {
			return chain.proceed(request);
		}
		mDebugger.applyDelayAfterBlacklist();

		final String uuid = UUID.randomUUID().toString();

		final NiddlerRequest niddlerRequest = new NiddlerOkHttpRequest(request, uuid);
		mNiddler.logRequest(niddlerRequest);

		Response debugResponse = makeResponse(mDebugger.handleRequest(niddlerRequest));

		final Response response = (debugResponse != null) ? debugResponse : chain.proceed(request);

		final long now = System.currentTimeMillis();
		final long sentAt = response.sentRequestAtMillis();
		final long receivedAt = response.receivedResponseAtMillis();
		final int wait = (int) (receivedAt - sentAt);
		final int writeTime = 0; //Unknown
		final int readTime = (int) (now - sentAt); //Unknown-ish

		final Response networkResponse = response.networkResponse();
		final Request networkRequest = (networkResponse == null) ? null : networkResponse.request();

		final NiddlerResponse niddlerResponse = new NiddlerOkHttpResponse(response, uuid,
				(networkRequest == null) ? null : new NiddlerOkHttpRequest(networkRequest, uuid),
				(networkResponse == null) ? null : new NiddlerOkHttpResponse(networkResponse, uuid, null, null, writeTime, readTime, wait),
				writeTime, readTime, wait);


		NiddlerDebugger.DebugResponse debugFromResponse = null;
		if (debugResponse == null) {
			debugFromResponse = mDebugger.handleResponse(niddlerRequest, niddlerResponse);
		}
		if (debugFromResponse == null) {
			mNiddler.logResponse(niddlerResponse);
			return response;
		} else {
			final int newWait = (int) (System.currentTimeMillis() - sentAt);
			final int newReadTime = (int) (System.currentTimeMillis() - sentAt);
			final Response debugResp = makeResponse(debugFromResponse);

			final NiddlerResponse debugNiddlerResponse = new NiddlerOkHttpResponse(debugResp, uuid,
					null,
					null,
					writeTime, newReadTime, newWait);

			mNiddler.logResponse(debugNiddlerResponse);
			return debugResp;
		}
	}

	private boolean isBlacklisted(@NonNull final CharSequence url) {
		for (final Pattern pattern : mBlacklist) {
			if (pattern.matcher(url).matches()) {
				return true;
			}
		}
		return mDebugger.isBlacklisted(url);
	}

	@Nullable
	private static Response makeResponse(@Nullable final NiddlerDebugger.DebugResponse debugResponse) {
		if (debugResponse == null) {
			return null;
		}

		final Response.Builder builder = new Response.Builder()
				.code(debugResponse.code)
				.message(debugResponse.message);

		if (debugResponse.headers != null) {
			builder.headers(Headers.of(debugResponse.headers));
		}
		builder.header(NIDDLER_DEBUG_RESPONSE_HEADER, "true");
		if (!TextUtils.isEmpty(debugResponse.encodedBody)) {
			builder.body(ResponseBody.create(MediaType.parse(debugResponse.bodyMimeType), Base64.decode(debugResponse.encodedBody, Base64.DEFAULT)));
		}
		builder.sentRequestAtMillis(System.currentTimeMillis());
		builder.receivedResponseAtMillis(System.currentTimeMillis());

		return builder.build();
	}
}
