package com.icapps.niddler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.util.Logging;
import com.icapps.niddler.util.OreoCompatHelper;

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
	private int mBindCount;
	private long mAutoStopAfter;
	private Handler mHandler;

	@Override
	public IBinder onBind(final Intent intent) {
		++mBindCount;
		mHandler.removeCallbacksAndMessages(null);
		return mBinder;
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		if (--mBindCount <= 0) {
			mBindCount = 0;

			if (mAutoStopAfter > 0) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						closeNiddler();
					}
				}, mAutoStopAfter);
			} else if (mAutoStopAfter == 0) {
				closeNiddler();
			}
		}
		return true;
	}

	@Override
	public void onRebind(final Intent intent) {
		++mBindCount;
		mHandler.removeCallbacksAndMessages(null);
		super.onRebind(intent);
	}

	public void initialize(final Niddler niddler, final long autoStopAfter) {
		if ((niddler == null) || niddler.isClosed()) {
			stopSelf();
			return;
		}
		mAutoStopAfter = autoStopAfter;
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
		mHandler = new Handler(Looper.getMainLooper());
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
		final String channelId;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			channelId = OreoCompatHelper.createNotificationChannel(this);
		} else {
			channelId = "";
		}

		final Intent intent = new Intent(this, NiddlerService.class);
		intent.setAction("STOP");
		final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		final Notification notification = new NotificationCompat.Builder(this, channelId)
				.setContentTitle("Niddler")
				.setContentText("Niddler is running. Touch to stop.")
				.setStyle(new NotificationCompat.BigTextStyle().bigText("Niddler is running for '" + getPackageName() + "' on port '" + mNiddler.getPort() + "'. Touch to stop"))
				.setContentIntent(pendingIntent)
				.setSmallIcon(android.R.drawable.ic_menu_preferences)
				.setLocalOnly(true)
				.build();

		notification.flags |= NotificationCompat.FLAG_NO_CLEAR | NotificationCompat.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void removeNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
