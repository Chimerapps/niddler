package com.icapps.niddler.interceptor.okhttp;

import androidx.annotation.NonNull;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Nicola Verbeeck
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerOkHttpInterceptor implements Interceptor {

	@Deprecated
	public NiddlerOkHttpInterceptor(@NonNull final Niddler niddler) {
		// Dummy implementation
	}

	/**
	 * Creates the authenticator that will report messages to the provided niddler. The name is only
	 * used for identification purposes on the client
	 *
	 * @param niddler The niddler instance to report to
	 * @param name    A name for this interceptor
	 */
	public NiddlerOkHttpInterceptor(@NonNull final Niddler niddler, @NonNull final String name) {
		// Dummy implementation
	}

	public NiddlerOkHttpInterceptor blacklist(@NonNull final String urlPattern) {
		//Dummy implementation
		return this;
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		return chain.proceed(chain.request());
	}

	@NonNull
	public static Request appendContext(@NonNull final Request request, @NonNull final String context) {
		return request;
	}

}
