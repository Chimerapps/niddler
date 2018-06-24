package com.icapps.niddler.util;

import android.support.annotation.RestrictTo;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public abstract class LogUtil {

	public static final int VERBOSE = 2;
	public static final int DEBUG = 3;
	public static final int INFO = 4;
	public static final int WARN = 5;
	public static final int ERROR = 6;

	public static LogUtil instance;

	public static boolean isLoggable(final String tag, final int level) {
		final LogUtil current = instance;
		return current != null && current.doIsLoggable(tag, level);
	}

	public static void niddlerLogDebug(final String tag, final String message) {
		final LogUtil current = instance;
		if (current != null) {
			current.doLog(DEBUG, tag, message, null);
		}
	}

	public static void niddlerLogInfo(final String tag, final String message) {
		final LogUtil current = instance;
		if (current != null) {
			current.doLog(INFO, tag, message, null);
		}
	}

	public static void niddlerLogWarning(final String tag, final String message) {
		niddlerLogWarning(tag, message, null);
	}

	public static void niddlerLogWarning(final String tag, final String message, final Throwable error) {
		final LogUtil current = instance;
		if (current != null) {
			current.doLog(WARN, tag, message, error);
		}
	}

	public static void niddlerLogError(final String tag, final String message) {
		niddlerLogError(tag, message, null);
	}

	public static void niddlerLogError(final String tag, final String message, final Throwable error) {
		final LogUtil current = instance;
		if (current != null) {
			current.doLog(ERROR, tag, message, error);
		}
	}

	public static void niddlerLogVerbose(final String tag, final String message) {
		final LogUtil current = instance;
		if (current != null) {
			current.doLog(VERBOSE, tag, message, null);
		}
	}

	protected abstract void doLog(final int level, final String tag, final String message, final Throwable error);

	protected abstract boolean doIsLoggable(final String tag, final int level);
}
