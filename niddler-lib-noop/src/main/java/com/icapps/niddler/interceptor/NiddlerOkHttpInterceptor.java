package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.Niddler;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

	public NiddlerOkHttpInterceptor(Niddler mNiddler) {
		// Dummy implementation
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		return chain.proceed(chain.request());
	}
}
