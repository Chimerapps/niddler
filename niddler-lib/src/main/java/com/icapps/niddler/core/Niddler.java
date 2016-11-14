package com.icapps.niddler.core;


import com.icapps.niddler.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import trikita.log.Log;

/**
 * @author Maarten Van Giel
 */
public final class Niddler {

    private final NiddlerServer mServer;

    private Niddler(int port) throws UnknownHostException {
        mServer = new NiddlerServer(port);
    }

    public void logRequest(final NiddlerRequest request) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.writeBody(os);
        String base64Body = Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);

        final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
        stringBuilder.append(request.getRequestId());
        stringBuilder.append("\", ");
        stringBuilder.append("\"url\":\"");
        stringBuilder.append(request.getUrl());
        stringBuilder.append("\", ");
        stringBuilder.append("\"method\":\"");
        stringBuilder.append(request.getMethod());
        stringBuilder.append("\", ");
        stringBuilder.append("\"body\":\"");
        stringBuilder.append(base64Body);
        stringBuilder.append("\", ");
        stringBuilder.append("\"headers\": {");

        final Map<String, List<String>> headerMap = request.getHeaders();
        final Iterator<String> headerIterator = headerMap.keySet().iterator();

        while (headerIterator.hasNext()) {
            final String headerName = headerIterator.next();
            final List<String> headers = headerMap.get(headerName);

            stringBuilder.append("\"");
            stringBuilder.append(headerName);
            stringBuilder.append("\": \"");

            for (String header : headers) {
                stringBuilder.append(header.replace("\"", "\\\"")); // This seems fragile...
                stringBuilder.append("\", ");
            }
            if (headers.size() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 2); // Remove trailing comma
            }

            if (headerIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("}}");
        mServer.sendToAll(stringBuilder.toString());
    }

    public void logResponse(final NiddlerResponse response) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.writeBody(os);
        String base64Body = Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);

        final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
        stringBuilder.append(response.getRequestId());
        stringBuilder.append("\", ");
        stringBuilder.append("\"statusCode\":");
        stringBuilder.append(response.getStatusCode());
        stringBuilder.append(", ");
        stringBuilder.append("\"body\":\"");
        stringBuilder.append(base64Body);
        stringBuilder.append("\", ");
        stringBuilder.append("\"headers\": {");

        final Map<String, List<String>> headerMap = response.getHeaders();
        final Iterator<String> headerIterator = headerMap.keySet().iterator();

        while (headerIterator.hasNext()) {
            final String headerName = headerIterator.next();
            final List<String> headers = headerMap.get(headerName);

            stringBuilder.append("\"");
            stringBuilder.append(headerName);
            stringBuilder.append("\": \"");

            for (String header : headers) {
                stringBuilder.append(header.replace("\"", "\\\"")); // This seems fragile...
                stringBuilder.append("\", ");
            }
            if (headers.size() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 2); // Remove trailing comma
            }

            if (headerIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("}}");
        mServer.sendToAll(stringBuilder.toString());
    }

    public void start() {
        mServer.start();
        Log.d("Started listening at address" + mServer.getAddress());
    }

    public void close() throws IOException, InterruptedException {
        mServer.stop();
    }

    public static class Builder {

        private int mPort = 6555;

        public Builder setPort(final int port) {
            mPort = port;
            return this;
        }

        public Niddler build() throws UnknownHostException {
            return new Niddler(mPort);
        }

    }

}
