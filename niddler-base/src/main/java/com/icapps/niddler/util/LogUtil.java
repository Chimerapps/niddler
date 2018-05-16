package com.icapps.niddler.util;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public class LogUtil {

	public static int WARN = 2;
	public static int VERBOSE = 4;

	public static boolean isLoggable(final String tag, final int level) {
		return false;
	}

	public static void logDebug(final String tag, final String message) {
	}

	public static void logInfo(final String tag, final String message) {
	}

	public static void logWarning(final String tag, final String message) {
	}

	public static void logWarning(final String tag, final String message, final Throwable error) {
	}

	public static void logError(final String tag, final String message) {
	}

	public static void logError(final String tag, final String message, final Throwable error) {
	}

	public static void logVerbose(final String tag, final String message) {

	}
}
