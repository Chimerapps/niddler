package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.icapps.niddler.core.Configuration;
import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.core.NiddlerRequest;

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
public class NiddlerOkHttpInterceptor implements Interceptor, Niddler.ConfigurationAware {

	private final Niddler mNiddler;
	private final List<Pattern> mBlacklist;
	@Nullable
	private volatile Configuration mConfiguration;

	public NiddlerOkHttpInterceptor(final Niddler niddler) {
		mNiddler = niddler;
		mBlacklist = new ArrayList<>();
		mConfiguration = null;
		niddler.registerConfigurationListener(this);
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

		final NiddlerRequest niddlerRequest = new NiddlerOkHttpRequest(request, uuid);
		mNiddler.logRequest(niddlerRequest);

		final Configuration config = mConfiguration;
		Response debugResponse = null;
		if (config != null && config.isActive()) {
			for (final Configuration.DebugAction debugAction : config.debugConfiguration.debugActions) {
				if (debugAction.handles(niddlerRequest)) {
					final Configuration.DebugResponse response = debugAction.handle(niddlerRequest);
					if (response != null) {
						debugResponse = makeResponse(response);
						break;
					}
				}
			}
		}

		final Response response = (debugResponse != null) ? debugResponse : chain.proceed(request);

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
		final Configuration config = mConfiguration;
		if (config != null && config.isActive()) {
			for (final Pattern regularExpression : config.blacklistConfiguration.regularExpressions) {
				if (regularExpression.matcher(url).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(@NonNull final Configuration configuration) {
		mConfiguration = configuration;
	}

	private static Response makeResponse(final Configuration.DebugResponse debugResponse) {
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
