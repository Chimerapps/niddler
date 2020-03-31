package com.icapps.niddler.util;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;


/**
 * @author Nicola Verbeeck
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class StringUtil {

	private StringUtil() {
		// Utility class
	}

	public static long calculateMemoryUsage(final String input) {
		return 8 * (((input.length()) * 2) + 45) / 8;
	}

	public static boolean isEmpty(@Nullable final CharSequence str) {
		return str == null || str.length() == 0;
	}

	public static byte[] fromBase64(final String body) {
		return Base64.decode(body);
	}

	public static String toString(byte[] data) {
		return Base64.encodeUrl(data);
	}

}
