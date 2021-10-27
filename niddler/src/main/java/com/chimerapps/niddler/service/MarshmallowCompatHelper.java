package com.chimerapps.niddler.service;

import android.app.PendingIntent;
import android.os.Build;

/**
 * @author Nicola Verbeeck
 */
final class MarshmallowCompatHelper {

	static int getPendingIntentFlags() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;
		}
		return PendingIntent.FLAG_CANCEL_CURRENT;
	}

}
