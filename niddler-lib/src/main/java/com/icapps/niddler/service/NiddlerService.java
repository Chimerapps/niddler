package com.icapps.niddler.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.icapps.niddler.core.Niddler;

import java.io.IOException;

/**
 * Created by maartenvangiel on 18/11/2016.
 */
public class NiddlerService extends Service {

	private static final int NOTIFICATION_ID = 1;

	private final IBinder mBinder = new NiddlerBinder();
	private NotificationManager mNotificationManager;
	private Niddler mNiddler;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void initialize(Niddler niddler) {
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals("STOP")) {
			closeNiddler();
		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		System.out.println("NiddlerService created!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		removeNotification();
		System.out.println("NiddlerService destroyed!");
	}

	private void closeNiddler() {
		if (mNiddler != null) {
			try {
				mNiddler.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			removeNotification();
		}
	}

	private void createNotification() {
		Intent intent = new Intent(this, NiddlerService.class);
		intent.setAction("STOP");
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification = new Notification.Builder(this)
				.setContentTitle("Niddler")
				.setContentText("Niddler is running. Touch to stop.")
				.setContentIntent(pendingIntent)
				.setSmallIcon(android.R.drawable.ic_menu_preferences)
				.build();

		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void removeNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
