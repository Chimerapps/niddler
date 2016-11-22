package com.icapps.niddler.util;

/**
 * @author Maarten Van Giel
 */
public final class StringSizeUtil {

	private StringSizeUtil() {
		// Utility class
	}

	public static long calculateMemoryUsage(final String input) {
		return 8 * (((input.length()) * 2) + 45) / 8;
	}

}
