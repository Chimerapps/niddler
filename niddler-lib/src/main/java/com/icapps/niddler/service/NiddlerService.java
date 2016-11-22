package com.icapps.niddler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.util.Logging;

import java.io.IOException;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
@SuppressWarnings("DesignForExtension")
public class NiddlerService extends Service {

	private static final String LOG_TAG = NiddlerService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 147;

	private final IBinder mBinder = new NiddlerBinder();
	private NotificationManager mNotificationManager;
	private Niddler mNiddler;

	@Override
	public IBinder onBind(final Intent intent) {
		return mBinder;
	}

	public void initialize(final Niddler niddler) {
		if (niddler == null || niddler.isClosed()) {
			stopSelf();
			return;
		}
		mNiddler = niddler;
		if (!mNiddler.isStarted()) {
			mNiddler.start();
		}
		createNotification();
	}

	public class NiddlerBinder extends Binder {
		public NiddlerService getService() {
			return NiddlerService.this;
		}
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals("STOP")) {
			closeNiddler();
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "NiddlerService created!");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		removeNotification();
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "NiddlerService destroyed!");
		}
	}

	private void closeNiddler() {
		if (mNiddler != null) {
			try {
				mNiddler.close();
			} catch (final IOException e) {
				Log.w(LOG_TAG, "Failed to close niddler", e);
			}
		}
		removeNotification();
	}

	private void createNotification() {
		final Intent intent = new Intent(this, NiddlerService.class);
		intent.setAction("STOP");
		final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		final Notification notification = new Notification.Builder(this)
				.setContentTitle("Niddler")
				.setContentText("Niddler is running for '" + getPackageName() + "'. Touch to stop.")
				.setContentIntent(pendingIntent)
				.setSmallIcon(android.R.drawable.ic_menu_preferences)
				.setLocalOnly(true)
				.build();

		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void removeNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
