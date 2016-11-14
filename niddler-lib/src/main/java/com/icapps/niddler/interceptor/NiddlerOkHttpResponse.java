package com.icapps.niddler.interceptor;

import com.icapps.niddler.core.NiddlerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public class NiddlerOkHttpResponse implements NiddlerResponse {

    private final Response mResponse;
    private final String mRequestId;

    public NiddlerOkHttpResponse(Response response, String requestId) {
        this.mResponse = response;
        this.mRequestId = requestId;
    }

    @Override
    public String getRequestId() {
        return mRequestId;
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
        try {
            final ResponseBody body = mResponse.body();
            if (body != null) {
                stream.write(body.bytes());
                stream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
