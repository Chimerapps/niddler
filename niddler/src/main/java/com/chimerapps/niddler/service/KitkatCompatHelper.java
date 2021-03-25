package com.chimerapps.niddler.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import androidx.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
class KitkatCompatHelper {

	static void setLocalOnly(@NonNull final Notification.Builder builder, final boolean localOnly) {
		builder.setLocalOnly(localOnly);
	}

}
