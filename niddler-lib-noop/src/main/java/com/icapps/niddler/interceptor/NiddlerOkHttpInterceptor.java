package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Maarten Van Giel
 */
@SuppressWarnings({"UnusedParameters", "unused"})
public class NiddlerOkHttpInterceptor implements Interceptor {

    public NiddlerOkHttpInterceptor(Niddler mNiddler) {
        // Dummy implementation
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
