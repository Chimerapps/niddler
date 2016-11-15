package com.icapps.niddler.util;

/**
 * Created by maartenvangiel on 15/11/2016.
 */
public final class StringSizeUtil {

    private StringSizeUtil() {
        // Utility class
    }

    public static long calculateMemoryUsage(final String input) {
        return 8 * (((input.length()) * 2) + 45) / 8;
    }

}
