package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

    private final Niddler mNiddler;

    public NiddlerOkHttpInterceptor(Niddler mNiddler) {
        this.mNiddler = mNiddler;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final String uuid = UUID.randomUUID().toString();

        final Request request = chain.request();
        mNiddler.logRequest(new NiddlerOkHttpRequest(request, uuid));

        final Response response = chain.proceed(request);
        mNiddler.logResponse(new NiddlerOkHttpResponse(response, uuid));

        return response;
    }
}
