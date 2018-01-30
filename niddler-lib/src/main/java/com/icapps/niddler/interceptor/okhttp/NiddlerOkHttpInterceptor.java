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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.icapps.niddler.core.Niddler.NIDDLER_DEBUG_RESPONSE_HEADER;
import static com.icapps.niddler.core.Niddler.NIDDLER_DEBUG_TIMING_RESPONSE_HEADER;

/**
 * @author Nicola Verbeeck
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

	private static final int FLAG_MODIFIED_RESPONSE = 1;
	private static final int FLAG_TIME = 2;

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
		final long callStartTime = System.nanoTime();

		final Request origRequest = chain.request();

		boolean changedTime = mDebugger.applyDelayBeforeBlacklist();
		if (isBlacklisted(origRequest.url().toString())) {
			return chain.proceed(origRequest);
		}
		changedTime |= mDebugger.applyDelayAfterBlacklist();

		final String uuid = UUID.randomUUID().toString();

		final NiddlerRequest origNiddlerRequest = new NiddlerOkHttpRequest(origRequest, uuid, buildExtraNiddlerHeaders(changedTime ? FLAG_TIME : 0));
		final NiddlerDebugger.DebugRequest overriddenRequest = mDebugger.overrideRequest(origNiddlerRequest);

		final Request finalRequest = (overriddenRequest == null) ? origRequest : makeRequest(overriddenRequest);

		final NiddlerRequest niddlerRequest = (overriddenRequest == null)
				? origNiddlerRequest : new NiddlerOkHttpRequest(finalRequest, uuid, buildExtraNiddlerHeaders(changedTime ? FLAG_TIME : 0));

		mNiddler.logRequest(niddlerRequest);

		final NiddlerDebugger.DebugResponse debuggerBeforeExecuteOverride = mDebugger.handleRequest(niddlerRequest);
		Response debugResponse = makeResponse(debuggerBeforeExecuteOverride);

		final Response response = (debugResponse != null) ? debugResponse : chain.proceed(finalRequest);

		final long now = System.currentTimeMillis();
		final long sentAt = response.sentRequestAtMillis();
		final long receivedAt = response.receivedResponseAtMillis();
		final int wait = (int) (receivedAt - sentAt);
		final int writeTime = 0; //Unknown
		final int readTime = (int) (now - sentAt); //Unknown-ish

		final Response networkResponse = response.networkResponse();
		final Request networkRequest = (networkResponse == null) ? null : networkResponse.request();

		changedTime = mDebugger.ensureCallTime(callStartTime);
		final Map<String, String> extraHeaders = buildExtraNiddlerHeaders((changedTime ? FLAG_TIME : 0) + (debuggerBeforeExecuteOverride != null ? FLAG_MODIFIED_RESPONSE : 0));

		final NiddlerResponse niddlerResponse = new NiddlerOkHttpResponse(response, uuid,
				(networkRequest == null) ? null : new NiddlerOkHttpRequest(networkRequest, uuid, null),
				(networkResponse == null) ? null : new NiddlerOkHttpResponse(networkResponse, uuid, null, null, writeTime, readTime, wait, null),
				writeTime, readTime, wait, extraHeaders);

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
					writeTime, newReadTime, newWait, buildExtraNiddlerHeaders(FLAG_MODIFIED_RESPONSE + (changedTime ? FLAG_TIME : 0)));

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

		if (!TextUtils.isEmpty(debugResponse.encodedBody)) {
			builder.body(ResponseBody.create(MediaType.parse(debugResponse.bodyMimeType), Base64.decode(debugResponse.encodedBody, Base64.DEFAULT)));
		}
		builder.sentRequestAtMillis(System.currentTimeMillis());
		builder.receivedResponseAtMillis(System.currentTimeMillis());

		return builder.build();
	}

	@NonNull
	private static Request makeRequest(@NonNull final NiddlerDebugger.DebugRequest debugRequest) {
		final RequestBody body;
		if (!TextUtils.isEmpty(debugRequest.encodedBody)) {
			body = RequestBody.create(MediaType.parse(debugRequest.bodyMimeType), Base64.decode(debugRequest.encodedBody, Base64.DEFAULT));
		} else {
			body = null;
		}

		final Request.Builder builder = new Request.Builder()
				.url(debugRequest.url)
				.method(debugRequest.method, body);
		if (debugRequest.headers != null) {
			builder.headers(Headers.of(debugRequest.headers));
		}
		return builder.build();
	}

	@Nullable
	private static Map<String, String> buildExtraNiddlerHeaders(final int flags) {
		if (flags == 0) {
			return null;
		}

		final Map<String, String> extra = new HashMap<>();
		if ((flags & FLAG_TIME) != 0) {
			extra.put(NIDDLER_DEBUG_TIMING_RESPONSE_HEADER, "true");
		}
		if ((flags & FLAG_MODIFIED_RESPONSE) != 0) {
			extra.put(NIDDLER_DEBUG_RESPONSE_HEADER, "true");
		}

		return extra;
	}
}
