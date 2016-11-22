package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.Niddler;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Maarten Van Giel
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

	private final Niddler mNiddler;

	public NiddlerOkHttpInterceptor(final Niddler niddler) {
		mNiddler = niddler;
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		final String uuid = UUID.randomUUID().toString();

		final Request request = chain.request();
		mNiddler.logRequest(new NiddlerOkHttpRequest(request, uuid));

		final Response response = chain.proceed(request);
		mNiddler.logResponse(new NiddlerOkHttpResponse(response, uuid));

		return response;
	}
}
