package com.icapps.niddler.util;

import android.support.annotation.RestrictTo;
import android.util.Log;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AndroidLogUtil extends LogUtil {

	@Override
	protected void doLog(final int level, final String tag, final String message, final Throwable error) {
		switch (level) {
			case VERBOSE:
				Log.v(tag, message, error);
				break;
			case DEBUG:
				Log.d(tag, message, error);
				break;
			case INFO:
				Log.i(tag, message, error);
				break;
			case WARN:
				Log.w(tag, message, error);
				break;
			case ERROR:
				Log.e(tag, message, error);
				break;
		}
	}

	@Override
	protected boolean doIsLoggable(final String tag, final int level) {
		return Log.isLoggable(tag, level);
	}

	@Override
	protected void doLogStartup(String message) {
		Log.i("Niddler", message);
	}
}
