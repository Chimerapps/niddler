package com.icapps.niddler.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
@TargetApi(Build.VERSION_CODES.O)
public class OreoCompatHelper {

	private static final String CHANNEL_ID = "Niddler-Channel-Id";
	private static final String CHANNEL_NAME = "Niddler Channel";
	private static final String CHANNEL_DESCRIPTION = "Channel for Niddler. Niddler uses a notification to keep it running and to notify the user that niddler is running";

	public static String createNotificationChannel(@NonNull final Context context) {
		final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		if (notificationManager == null) {
			return "";
		}

		final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
		channel.setDescription(CHANNEL_DESCRIPTION);
		channel.enableLights(false);
		channel.enableVibration(false);
		channel.setBypassDnd(false);
		channel.setShowBadge(false);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

		notificationManager.createNotificationChannel(channel);
		return CHANNEL_ID;
	}

}
