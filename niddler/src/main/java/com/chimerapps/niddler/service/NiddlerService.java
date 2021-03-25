package com.chimerapps.niddler.service;

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
import android.util.Log;

import com.chimerapps.niddler.R;
import com.chimerapps.niddler.core.Niddler;

import java.io.IOException;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
public class NiddlerService extends Service {

	private static final String LOG_TAG = NiddlerService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 147;
	private static final long PORT_WAIT_DELAY = 500L;

	private IBinder mBinder;
	private NotificationManager mNotificationManager;
	private Niddler mNiddler;
	private int mBindCount;
	private long mAutoStopAfter;
	private Handler mHandler;
	private final Handler mNotificationPrepareHandler = new Handler(Looper.getMainLooper());

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
		mBinder = new NiddlerBinder();
		mHandler = new Handler(Looper.getMainLooper());
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Log.d(LOG_TAG, "NiddlerService created!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		removeNotification();
		mBinder = null;
		Log.d(LOG_TAG, "NiddlerService destroyed!");
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
		mNotificationPrepareHandler.removeCallbacksAndMessages(null);
		if (mNiddler.getPort() == 0) {
			mNotificationPrepareHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					createNotification();
				}
			}, PORT_WAIT_DELAY);
			return;
		}

		final Notification.Builder notificationBuilder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationBuilder = OreoCompatHelper.createNotificationBuilder(this);
		} else {
			notificationBuilder = new Notification.Builder(this);
		}

		final Intent intent = new Intent(this, NiddlerService.class);
		intent.setAction("STOP");
		final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		notificationBuilder.setContentTitle("Niddler")
				.setContentText(getString(R.string.niddler_running_notification));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			JellyBeanCompatHelper.addBigText(notificationBuilder, getString(R.string.niddler_running_notification_big, getPackageName(), mNiddler.getPort()));
		}

		notificationBuilder.setContentIntent(pendingIntent)
				.setSmallIcon(android.R.drawable.ic_menu_preferences);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			KitkatCompatHelper.setLocalOnly(notificationBuilder, true);
		}

		final Notification notification = JellyBeanCompatHelper.build(notificationBuilder);

		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void removeNotification() {
		mNotificationPrepareHandler.removeCallbacksAndMessages(null);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
