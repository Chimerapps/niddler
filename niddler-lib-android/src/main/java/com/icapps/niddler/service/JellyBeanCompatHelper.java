package com.icapps.niddler.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
final class JellyBeanCompatHelper {

	static void addBigText(@NonNull final Notification.Builder builder, @NonNull final String text) {
		builder.setStyle(new Notification.BigTextStyle().bigText(text));
	}

	@NonNull
	static Notification build(@NonNull final Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			return builder.build();
		}
		//noinspection deprecation
		return builder.getNotification();
	}

}
