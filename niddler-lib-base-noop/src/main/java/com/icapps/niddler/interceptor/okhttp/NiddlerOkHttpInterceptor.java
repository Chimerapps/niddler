package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.NonNull;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Nicola Verbeeck
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerOkHttpInterceptor implements Interceptor {

    public NiddlerOkHttpInterceptor(final Niddler niddler) {
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
}
