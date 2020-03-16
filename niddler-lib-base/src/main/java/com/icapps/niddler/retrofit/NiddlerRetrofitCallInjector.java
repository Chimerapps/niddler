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
	 * @param builder     The builder to update
	 * @param niddler     The niddler instance
	 * @param callFactory The actual factory for creating calls
	 * @return The builder itself
	 */
	@NonNull
	public static Retrofit.Builder inject(@NonNull final Retrofit.Builder builder, @NonNull final Niddler niddler, @NonNull final Call.Factory callFactory) {
		return inject(builder, niddler, callFactory, DEFAULT_SKIP);
	}

	/**
	 * Modified the retrofit builder to allow including stack traces into requests.
	 *
	 * @param builder     The builder to update
	 * @param niddler     The niddler instance
	 * @param callFactory The actual factory for creating calls
	 * @param skipPast    The number of stack trace entries to skip past
	 * @return The builder itself
	 */
	@NonNull
	public static Retrofit.Builder inject(@NonNull final Retrofit.Builder builder, @NonNull final Niddler niddler, @NonNull final Call.Factory callFactory, final int skipPast) {
		if (!niddler.isStackTracingEnabled()) {
			return builder.callFactory(callFactory);
		}

		builder.callFactory(new okhttp3.Call.Factory() {
			@Override
			public okhttp3.Call newCall(final Request request) {
				if (request.tag(Niddler.StackTraceKey.class) != null) {
					return callFactory.newCall(request);
				}

				final Throwable e = new IllegalStateException();
				final StackTraceElement[] trace = e.getStackTrace();

				final StackTraceElement[] skipped;
				if (trace.length > skipPast && skipPast > 0) {
					skipped = new StackTraceElement[trace.length - skipPast];
					System.arraycopy(trace, skipPast, skipped, 0, skipped.length);
				} else {
					skipped = trace;
				}
				final Niddler.StackTraceKey id = niddler.pushStackTrace(skipped);
				return callFactory.newCall(request.newBuilder().tag(Niddler.StackTraceKey.class, id).build());
			}
		});

		return builder;
	}

}
