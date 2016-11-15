package com.icapps.niddler.core;


import com.icapps.niddler.util.Base64;
import com.icapps.niddler.util.StringSizeUtil;

import org.java_websocket.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import trikita.log.Log;

/**
 * @author Maarten Van Giel
 */
public final class Niddler implements NiddlerServer.WebSocketListener {

    private final long mMaxCacheSize;
    private final List<String> mMessageCache;
    private final NiddlerServer mServer;

    private long mCacheSize = 0;

    private Niddler(final int port, final long cacheSize) throws UnknownHostException {
        mServer = new NiddlerServer(port, this);
        mMessageCache = new LinkedList<>();
        mMaxCacheSize = cacheSize;
    }

    public void logRequest(final NiddlerRequest request) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.writeBody(os);
        String base64Body = Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);

        final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
        stringBuilder.append(request.getRequestId());
        stringBuilder.append("\", \"messageId\":\"");
        stringBuilder.append(request.getMessageId());
        stringBuilder.append("\", \"url\":\"");
        stringBuilder.append(request.getUrl());
        stringBuilder.append("\", \"timestamp\":");
        stringBuilder.append(request.getTimestamp());
        stringBuilder.append(", \"method\":\"");
        stringBuilder.append(request.getMethod());
        stringBuilder.append("\", \"body\":\"");
        stringBuilder.append(base64Body);
        stringBuilder.append("\", \"headers\": {");

        final Map<String, List<String>> headerMap = request.getHeaders();
        final Iterator<String> headerIterator = headerMap.keySet().iterator();

        while (headerIterator.hasNext()) {
            final String headerName = headerIterator.next();
            final List<String> headers = headerMap.get(headerName);

            stringBuilder.append("\"");
            stringBuilder.append(headerName);
            stringBuilder.append("\": [");

            for (String header : headers) {
                stringBuilder.append("\"");
                stringBuilder.append(header.replace("\"", "\\\"")); // This seems fragile...
                stringBuilder.append("\", ");
            }
            if (headers.size() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 2); // Remove trailing comma
            }

            stringBuilder.append("]");

            if (headerIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("}}");
        sendWithCache(stringBuilder.toString());
    }

    public void logResponse(final NiddlerResponse response) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.writeBody(os);
        String base64Body = Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);

        final StringBuilder stringBuilder = new StringBuilder("{\"requestId\":\"");
        stringBuilder.append(response.getRequestId());
        stringBuilder.append("\", \"messageId\":\"");
        stringBuilder.append(response.getMessageId());
        stringBuilder.append("\", \"timestamp\":");
        stringBuilder.append(response.getTimestamp());
        stringBuilder.append(", \"statusCode\":");
        stringBuilder.append(response.getStatusCode());
        stringBuilder.append(", \"body\":\"");
        stringBuilder.append(base64Body);
        stringBuilder.append("\", \"headers\": {");

        final Map<String, List<String>> headerMap = response.getHeaders();
        final Iterator<String> headerIterator = headerMap.keySet().iterator();

        while (headerIterator.hasNext()) {
            final String headerName = headerIterator.next();
            final List<String> headers = headerMap.get(headerName);

            stringBuilder.append("\"");
            stringBuilder.append(headerName);
            stringBuilder.append("\": [");

            for (String header : headers) {
                stringBuilder.append("\"");
                stringBuilder.append(header.replace("\"", "\\\"")); // This seems fragile...
                stringBuilder.append("\", ");
            }
            if (headers.size() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 2); // Remove trailing comma
            }

            stringBuilder.append("]");

            if (headerIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("}}");
        sendWithCache(stringBuilder.toString());
    }

    public void start() {
        mServer.start();
        Log.d("Started listening at address" + mServer.getAddress());
    }

    public void close() throws IOException, InterruptedException {
        mServer.stop();
    }

    private void sendWithCache(final String message){
        if(!mServer.connections().isEmpty()){
            mServer.sendToAll(message);
            return;
        }

        if(mMaxCacheSize <= 0){
            return;
        }

        final long messageMemoryUsage = StringSizeUtil.calculateMemoryUsage(message);
        if(mCacheSize + messageMemoryUsage < mMaxCacheSize){
            mMessageCache.add(message);
            mCacheSize += messageMemoryUsage;
        } else {
            if(messageMemoryUsage > mMaxCacheSize){
                Log.d("Message too long for cache");
            } else {
                Log.d("Cache is full, removing items until we have enough space");
                while(mCacheSize + messageMemoryUsage >= mMaxCacheSize){
                    final String oldestMessage = mMessageCache.get(0);
                    mCacheSize -= StringSizeUtil.calculateMemoryUsage(oldestMessage);
                    mMessageCache.remove(oldestMessage);
                }
                mMessageCache.add(message);
                mCacheSize += messageMemoryUsage;
            }
        }
    }

    @Override
    public void onConnectionOpened(WebSocket conn) {
        Iterator<String> messageIterator = mMessageCache.iterator();
        while(messageIterator.hasNext()){
            conn.send(messageIterator.next());
            messageIterator.remove();
        }
    }

    public final static class Builder {

        private int mPort = 6555;
        private long mCacheSize = 1048500; // By default use 1 MB cache

        /**
         * Sets the port on which Niddler will listen for incoming connections
         * @param port The port to be used
         * @return Builder
         */
        public Builder setPort(final int port) {
            mPort = port;
            return this;
        }

        /**
         * Sets the cache size to be used for caching requests and responses while there is no client connected
         * @param cacheSize The cache size to be used, in bytes
         * @return Builder
         */
        public Builder setCacheSize(final long cacheSize) {
            mCacheSize = cacheSize;
            return this;
        }

        /**
         * Builds a Niddler instance with the configured parameters
         * @return a Niddler instance
         * @throws UnknownHostException
         */
        public Niddler build() throws UnknownHostException {
            return new Niddler(mPort, mCacheSize);
        }

    }

}
