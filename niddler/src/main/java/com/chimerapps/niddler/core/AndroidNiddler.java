package com.chimerapps.niddler.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.chimerapps.niddler.service.NiddlerService;
import com.chimerapps.niddler.util.AndroidLogUtil;
import com.chimerapps.niddler.util.LogUtil;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public final class AndroidNiddler extends Niddler implements Niddler.PlatformNiddler {

	private long mAutoStopAfter = -1;
	private NiddlerServiceLifeCycleWatcher mLifeCycleWatcher = null;

	private NiddlerService mNiddlerService;

	private AndroidNiddler(final String password, final int port, final long cacheSize,
			final NiddlerServerInfo niddlerServerInfo, final int maxStackTraceSize) {
		super(password, port, cacheSize, niddlerServerInfo, maxStackTraceSize);
		mNiddlerImpl.setPlatform(this);
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application the application to attach the Niddler instance to
	 */
	@SuppressWarnings("WeakerAccess")
	public void attachToApplication(final Application application) {
		attachToApplication(application, -1L);
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application   the application to attach the Niddler instance to
	 * @param autoStopAfter Automatically stop the niddler background service after x milliseconds. Use -1 to keep the service running and use 0 to stop the service immediately
	 */
	@SuppressWarnings("WeakerAccess")
	public void attachToApplication(final Application application, final long autoStopAfter) {
		mAutoStopAfter = autoStopAfter;
		if (mLifeCycleWatcher == null) {
			final Niddler niddler = this;
			mLifeCycleWatcher = new NiddlerServiceLifeCycleWatcher(new ServiceConnection() {
				@Override
				public void onServiceConnected(final ComponentName name, final IBinder service) {
					if (!(service instanceof NiddlerService.NiddlerBinder)) //We are under test or something?
					{
						return;
					}

					mNiddlerService = ((NiddlerService.NiddlerBinder) service).getService();
					mNiddlerService.initialize(niddler, mAutoStopAfter);
				}

				@Override
				public void onServiceDisconnected(final ComponentName name) {
					mNiddlerService = null;
				}
			}, niddler);
		}
		application.unregisterActivityLifecycleCallbacks(mLifeCycleWatcher);
		application.registerActivityLifecycleCallbacks(mLifeCycleWatcher);
	}

	@Override
	public void closePlatform() {
		final NiddlerService niddlerService = mNiddlerService;
		if (niddlerService != null) {
			niddlerService.stopSelf();
			mNiddlerService = null;
		}
	}

	/**
	 * Creates a server info based on the application's package name and some device fields.
	 * To provide a session icon, you can use meta data in the AndroidManifest. Eg: {@code <meta-data android:name="com.niddler.icon" android:value="android"/>}.
	 * Icons can be names which will be resolved inside the UI or from plugin 2.9.9 and up base64 encoded images
	 *
	 * @param application The application niddler is instrumenting
	 * @return A server info document to use in the {@link Niddler.Builder}
	 */
	public static Niddler.NiddlerServerInfo fromApplication(final Application application) {
		return new Niddler.NiddlerServerInfo(application.getPackageName(), Build.MANUFACTURER + " " + Build.PRODUCT, getIconFromMeta(application));
	}

	@Nullable
	private static String getIconFromMeta(final Application application) {
		try {
			final ApplicationInfo app = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
			return app.metaData.getString("com.niddler.icon");
		} catch (final Throwable e) {
			return null;
		}
	}

	public static class Builder extends Niddler.Builder<AndroidNiddler> {

		public Builder() {
			LogUtil.instance = new AndroidLogUtil();
		}

		@Override
		public AndroidNiddler build() {
			return new AndroidNiddler(mPassword, mPort, mCacheSize, mNiddlerServerInfo, mMaxStackTraceSize);
		}
	}
}
