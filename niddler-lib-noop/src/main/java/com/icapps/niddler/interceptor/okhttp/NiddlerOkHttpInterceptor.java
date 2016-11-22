package com.icapps.niddler.interceptor.okhttp;

import com.icapps.niddler.core.Niddler;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author Maarten Van Giel
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerOkHttpInterceptor implements Interceptor {

	public NiddlerOkHttpInterceptor(final Niddler niddler) {
		// Dummy implementation
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		return chain.proceed(chain.request());
	}
}
