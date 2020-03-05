package com.icapps.niddler.interceptor.okhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;
import com.icapps.niddler.core.debug.NiddlerDebugger;
import com.icapps.niddler.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.icapps.niddler.core.Niddler.NIDDLER_DEBUG_RESPONSE_HEADER;
import static com.icapps.niddler.core.Niddler.NIDDLER_DEBUG_TIMING_RESPONSE_HEADER;

/**
 * @author Nicola Verbeeck
 */
public class NiddlerOkHttpInterceptor implements Interceptor {

    private static final int FLAG_MODIFIED_RESPONSE = 1;
    private static final int FLAG_TIME              = 2;
    private static final int FLAG_MODIFIED_REQUEST  = 4;

    @NonNull
    private final Niddler                            mNiddler;
    @NonNull
    private final String                             mName;
    @NonNull
    private final String                             mId;
    private final List<Niddler.StaticBlackListEntry> mBlacklist;
    @NonNull
    private final NiddlerDebugger                    mDebugger;

    /**
     * Deprecated, use {@link #NiddlerOkHttpInterceptor(Niddler, String)} instead
     */
    @Deprecated
    public NiddlerOkHttpInterceptor(@NonNull final Niddler niddller) {
        this(niddller, "<No name>");
    }

    /**
     * Creates the authenticator that will report messages to the provided niddler. The name is only
     * used for identification purposes on the client
     *
     * @param niddler The niddler instance to report to
     * @param name    A name for this interceptor
     */
    public NiddlerOkHttpInterceptor(@NonNull final Niddler niddler, @NonNull final String name) {
        mNiddler = niddler;
        mBlacklist = new CopyOnWriteArrayList<>();
        mDebugger = niddler.debugger();
        mName = name;
        mId = UUID.randomUUID().toString();

        mNiddler.registerBlacklistListener(new Niddler.StaticBlacklistListener() {

            @NonNull
            @Override
            public String getId() {
                return mId;
            }

            @Override
            public void setBlacklistItemEnabled(@NonNull final String pattern, final boolean enabled) {
                NiddlerOkHttpInterceptor.this.setBlacklistItemEnabled(pattern, enabled);
            }
        });
    }

    /**
     * Adds a static blacklist on the given url pattern. The pattern is interpreted as a java regex ({@link Pattern}). Items matching the blacklist are not tracked by niddler.
     * This blacklist is independent from any debugger blacklists
     *
     * @param urlPattern The pattern to add to the blacklist
     * @return This instance
     */
    @SuppressWarnings("unused")
    public NiddlerOkHttpInterceptor blacklist(@NonNull final String urlPattern) {
        mBlacklist.add(new Niddler.StaticBlackListEntry(urlPattern));
        mNiddler.onStaticBlacklistChanged(mId, mName, mBlacklist);
        return this;
    }

    /**
     * Allows you to enable/disable static blacklist items based on the pattern. This only affects the static blacklist, independent from debugger blacklists
     *
     * @param pattern The pattern to enable/disable in the blacklist. If a pattern is added that does not exist yet in the blacklist, it is added
     * @param enabled Flag indicating if the static blacklist item should be enabled or disabled
     */
    private void setBlacklistItemEnabled(@NonNull final String pattern, final boolean enabled) {
        boolean modified = false;
        for (final Niddler.StaticBlackListEntry blackListEntry : mBlacklist) {
            if (blackListEntry.isForPattern(pattern)) {
                if (blackListEntry.setEnabled(enabled)) {
                    modified = true;
                }
            }
        }
        if (!modified) {
            final Niddler.StaticBlackListEntry entry = new Niddler.StaticBlackListEntry(pattern);
            entry.setEnabled(enabled);
            mBlacklist.add(entry);
        }
        mNiddler.onStaticBlacklistChanged(mId, mName, mBlacklist);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final long callStartTime = System.nanoTime();

        final Request origRequest = chain.request();
        final StackTraceElement[] traces;
        final Niddler.StackTraceKey traceKey = origRequest.tag(Niddler.StackTraceKey.class);
        final NiddlerRequestContext requestContext = origRequest.tag(NiddlerRequestContext.class);
        if (traceKey == null) {
            traces = null;
        } else {
            traces = mNiddler.popTraceForId(traceKey);
        }

        boolean changedTime = mDebugger.applyDelayBeforeBlacklist();
        if (isBlacklisted(origRequest.url().toString())) {
            return chain.proceed(origRequest);
        }
        changedTime |= mDebugger.applyDelayAfterBlacklist();

        final String uuid = UUID.randomUUID().toString();

        final NiddlerRequest origNiddlerRequest = new NiddlerOkHttpRequest(origRequest, uuid, buildExtraNiddlerHeaders(changedTime ? FLAG_TIME : 0), traces, requestContext);
        final NiddlerDebugger.DebugRequest overriddenRequest = mDebugger.overrideRequest(origNiddlerRequest);

        final Request finalRequest = (overriddenRequest == null) ? origRequest : makeRequest(overriddenRequest);

        final NiddlerRequest niddlerRequest = (overriddenRequest == null)
                ? origNiddlerRequest : new NiddlerOkHttpRequest(finalRequest, uuid, buildExtraNiddlerHeaders((changedTime ? FLAG_TIME : 0) + FLAG_MODIFIED_REQUEST), traces, requestContext);

        mNiddler.logRequest(niddlerRequest);

        final NiddlerDebugger.DebugResponse debuggerBeforeExecuteOverride = mDebugger.handleRequest(niddlerRequest);
        Response debugResponse = makeResponse(debuggerBeforeExecuteOverride, finalRequest, null);

        final Response response = (debugResponse != null) ? debugResponse : chain.proceed(finalRequest);

        final long now = System.currentTimeMillis();
        final long sentAt = response.sentRequestAtMillis();
        final long receivedAt = response.receivedResponseAtMillis();
        final int wait = (int) (receivedAt - sentAt);
        final int writeTime = 0; //Unknown
        final int readTime = (int) (now - sentAt); //Unknown-ish

        final Response networkResponse = response.networkResponse();
        final Request networkRequest = (networkResponse == null) ? null : networkResponse.request();

        changedTime = mDebugger.ensureCallTime(callStartTime);
        final Map<String, String> extraHeaders = buildExtraNiddlerHeaders((changedTime ? FLAG_TIME : 0) + (debuggerBeforeExecuteOverride != null ? FLAG_MODIFIED_RESPONSE : 0));

        final NiddlerResponse niddlerResponse = new NiddlerOkHttpResponse(response, uuid,
                (networkRequest == null) ? null : new NiddlerOkHttpRequest(networkRequest, uuid, null, null, null),
                (networkResponse == null) ? null : new NiddlerOkHttpResponse(networkResponse, uuid, null, null, writeTime, readTime, wait, null),
                writeTime, readTime, wait, extraHeaders);

        NiddlerDebugger.DebugResponse debugFromResponse = null;
        if (debugResponse == null) {
            debugFromResponse = mDebugger.handleResponse(niddlerRequest, niddlerResponse);
        }
        if (debugFromResponse == null) {
            mNiddler.logResponse(niddlerResponse);
            return response;
        } else {
            final int newWait = (int) (System.currentTimeMillis() - sentAt);
            final int newReadTime = (int) (System.currentTimeMillis() - sentAt);
            final Response debugResp = makeResponse(debugFromResponse, response.request(), response);

            final NiddlerResponse debugNiddlerResponse = new NiddlerOkHttpResponse(debugResp, uuid,
                    null,
                    null,
                    writeTime, newReadTime, newWait, buildExtraNiddlerHeaders(FLAG_MODIFIED_RESPONSE + (changedTime ? FLAG_TIME : 0)));

            mNiddler.logResponse(debugNiddlerResponse);
            return debugResp;
        }
    }

    private boolean isBlacklisted(@NonNull final CharSequence url) {
        for (final Niddler.StaticBlackListEntry entry : mBlacklist) {
            if (entry.matches(url)) {
                return true;
            }
        }
        return mDebugger.isBlacklisted(url);
    }

    @Nullable
    private static Response makeResponse(@Nullable final NiddlerDebugger.DebugResponse debugResponse, final Request request, @Nullable final Response response) {
        if (debugResponse == null) {
            return null;
        }

        final Response.Builder builder = new Response.Builder()
                .code(debugResponse.code)
                .message(debugResponse.message);

        if (debugResponse.headers != null) {
            final Headers.Builder headers = new Headers.Builder();
            for (final Map.Entry<String, List<String>> entry : debugResponse.headers.entrySet()) {
                for (final String value : entry.getValue()) {
                    headers.add(entry.getKey(), value);
                }
            }
            builder.headers(headers.build());
        }

        if (!StringUtil.isEmpty(debugResponse.encodedBody)) {
            builder.body(ResponseBody.create(MediaType.parse(debugResponse.bodyMimeType), StringUtil.fromBase64(debugResponse.encodedBody)));
        }
        builder.sentRequestAtMillis(System.currentTimeMillis());
        builder.request(request);
        builder.receivedResponseAtMillis(System.currentTimeMillis());

        if (response == null) {
            builder.protocol(Protocol.HTTP_1_1);
        } else {
            builder.protocol(response.protocol());
        }

        return builder.build();
    }

    @NonNull
    private static Request makeRequest(@NonNull final NiddlerDebugger.DebugRequest debugRequest) {
        final RequestBody body;
        if (!StringUtil.isEmpty(debugRequest.encodedBody)) {
            body = RequestBody.create(MediaType.parse(debugRequest.bodyMimeType), StringUtil.fromBase64(debugRequest.encodedBody));
        } else {
            body = null;
        }

        final Request.Builder builder = new Request.Builder()
                .url(debugRequest.url)
                .method(debugRequest.method, body);
        if (debugRequest.headers != null) {
            final Headers.Builder headers = new Headers.Builder();
            for (final Map.Entry<String, List<String>> entry : debugRequest.headers.entrySet()) {
                for (final String value : entry.getValue()) {
                    headers.add(entry.getKey(), value);
                }
            }
            builder.headers(headers.build());
        }
        return builder.build();
    }

    @Nullable
    private static Map<String, String> buildExtraNiddlerHeaders(final int flags) {
        if (flags == 0) {
            return null;
        }

        final Map<String, String> extra = new HashMap<>();
        if ((flags & FLAG_TIME) != 0) {
            extra.put(NIDDLER_DEBUG_TIMING_RESPONSE_HEADER, "true");
        }
        if ((flags & FLAG_MODIFIED_RESPONSE) != 0 || (flags & FLAG_MODIFIED_REQUEST) != 0) {
            extra.put(NIDDLER_DEBUG_RESPONSE_HEADER, "true");
        }

        return extra;
    }

    @NonNull
    public static Request appendContext(@NonNull final Request request, @NonNull final String context) {
        NiddlerRequestContext existingContext = request.tag(NiddlerRequestContext.class);
        final Request requestToReturn;
        if (existingContext == null) {
            existingContext = new NiddlerRequestContext(new ArrayList<String>());
            requestToReturn = request.newBuilder().tag(NiddlerRequestContext.class, existingContext).build();
        } else {
            requestToReturn = request;
        }
        existingContext.appendContext(context);
        return requestToReturn;
    }

    static class NiddlerRequestContext {
        final List<String> mContextInformation;

        NiddlerRequestContext(final List<String> contextInformation) {
            mContextInformation = contextInformation;
        }

        void appendContext(@NonNull final String context) {
            mContextInformation.add(context);
        }

        List<String> getContextInformation() {
            return mContextInformation;
        }
    }

}
