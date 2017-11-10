package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.NonNull;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Nicola Verbeeck
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

	private final Niddler mNiddler;
	private final List<Pattern> mBlacklist;

	public NiddlerOkHttpInterceptor(final Niddler niddler) {
		mNiddler = niddler;
		mBlacklist = new ArrayList<>();
	}

	public NiddlerOkHttpInterceptor blacklist(@NonNull final String urlPattern) {
		mBlacklist.add(Pattern.compile(urlPattern));
		return this;
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		final Request request = chain.request();
		if (isBlacklisted(request.url().toString())) {
			return chain.proceed(request);
		}

		final String uuid = UUID.randomUUID().toString();

		mNiddler.logRequest(new NiddlerOkHttpRequest(request, uuid));

		final Response response = chain.proceed(request);

		final long now = System.currentTimeMillis();
		final long sentAt = response.sentRequestAtMillis();
		final long receivedAt = response.receivedResponseAtMillis();
		final int wait = (int) (receivedAt - sentAt);
		final int writeTime = 0; //Unknown
		final int readTime = (int) (now - sentAt); //Unknown-ish

		final Response networkResponse = response.networkResponse();
		final Request networkRequest = (networkResponse == null) ? null : networkResponse.request();
		mNiddler.logResponse(new NiddlerOkHttpResponse(response,
				uuid,
				(networkRequest == null) ? null : new NiddlerOkHttpRequest(networkRequest, uuid),
				(networkResponse == null) ? null : new NiddlerOkHttpResponse(networkResponse, uuid, null, null, writeTime, readTime, wait),
				writeTime, readTime, wait));

		return response;
	}

	private boolean isBlacklisted(@NonNull final CharSequence url) {
		for (final Pattern pattern : mBlacklist) {
			if (pattern.matcher(url).matches()) {
				return true;
			}
		}
		return false;
	}
}
