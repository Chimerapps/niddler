package com.icapps.niddler.core;

import android.app.Application;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public final class AndroidNiddler extends Niddler implements Niddler.PlatformNiddler {

	private AndroidNiddler() {
		super();
	}

	/**
	 * Attaches the Niddler instance to the application's activity lifecycle callbacks, thus starting and stopping a NiddlerService
	 * when activities start and stop. This will show a notification with which you can stop Niddler at any time.
	 *
	 * @param application the application to attach the Niddler instance to
	 */
	@SuppressWarnings("WeakerAccess")
	public void attachToApplication(final Application application) {
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
	}

	@Override
	public void closePlatform() {
	}

	/**
	 * Creates a server info based on the application's package name and some device fields.
	 * To provide a session icon, you can use meta data in the AndroidManifest. Eg: {@code <meta-data android:name="com.niddler.icon" android:value="android"/>}
	 *
	 * @param application The application niddler is instrumenting
	 * @return A server info document to use in the {@link Niddler.Builder}
	 */
	public static NiddlerServerInfo fromApplication(final Application application) {
		return new NiddlerServerInfo("", "");
	}

	public static class Builder extends Niddler.Builder<AndroidNiddler> {

		public Builder() {
		}

		@Override
		public AndroidNiddler build() {
			return new AndroidNiddler();
		}
	}

}