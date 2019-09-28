package com.icapps.niddler.retrofit;

import android.support.annotation.NonNull;

import com.icapps.niddler.core.Niddler;

import okhttp3.Call;
import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * Helper utility class that injects the call-site stack trace for {@link com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor} for retrofit
 */
public final class NiddlerRetrofitCallInjector {

    private static final int DEFAULT_SKIP = 4;

    private NiddlerRetrofitCallInjector() {
        //Utility class
    }

    /**
     * Modified the retrofit builder to allow including stack traces into requests. Uses the default skip stack size (4) to declutter the request
     *
     * @param builder   The builder to update
     * @param niddler   The niddler instance
     * @param callFactory   The actual factory for creating calls
     * @return The builder itself
     */
    @NonNull
    public static Retrofit.Builder inject(@NonNull final Retrofit.Builder builder, @NonNull final Niddler niddler, @NonNull final Call.Factory callFactory) {
        return builder.callFactory(callFactory);
    }

    /**
     * Modified the retrofit builder to allow including stack traces into requests.
     *
     * @param builder   The builder to update
     * @param niddler   The niddler instance
     * @param callFactory   The actual factory for creating calls
     * @param skipPast  The number of stack trace entries to skip past
     * @return The builder itself
     */
    @NonNull
    public static Retrofit.Builder inject(@NonNull final Retrofit.Builder builder, @NonNull final Niddler niddler, @NonNull final Call.Factory callFactory, final int skipPast) {
        return builder.callFactory(callFactory);
    }

}
