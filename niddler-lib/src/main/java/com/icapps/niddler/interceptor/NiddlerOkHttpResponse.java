package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.NiddlerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpResponse implements NiddlerResponse {

    private final Response mResponse;
    private final String mRequestId;
    private final String mMessageId;
    private final long mTimestamp;

    public NiddlerOkHttpResponse(Response response, String requestId) {
        this.mResponse = response;
        this.mRequestId = requestId;
        this.mMessageId = UUID.randomUUID().toString();
        mTimestamp = System.currentTimeMillis();
    }

    @Override
    public String getMessageId() {
        return mMessageId;
    }

    @Override
    public String getRequestId() {
        return mRequestId;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return mResponse.headers().toMultimap();
    }

    @Override
    public Integer getStatusCode() {
        return mResponse.code();
    }

    @Override
    public void writeBody(OutputStream stream) {
        final ResponseBody body = mResponse.body();
        try {
            if (body != null) {
                final BufferedSource source = body.source();
                source.request(Long.MAX_VALUE); // Buffer entire body

                final Buffer buffer = source.buffer();
                stream.write(buffer.clone().readByteArray());
                stream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
