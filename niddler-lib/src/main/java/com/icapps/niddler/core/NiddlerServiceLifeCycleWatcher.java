package com.icapps.niddler.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import com.icapps.niddler.service.NiddlerService;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
class NiddlerServiceLifeCycleWatcher implements  Application.ActivityLifecycleCallbacks{

	private final ServiceConnection mServiceConnection;

	NiddlerServiceLifeCycleWatcher(final ServiceConnection connection){
		mServiceConnection = connection;
	}

	@Override
	public void onActivityCreated(final Activity activity, final Bundle bundle) {
		//Not handled
	}

	@Override
	public void onActivityStarted(final Activity activity) {
		final Intent serviceIntent = new Intent(activity, NiddlerService.class);
		activity.startService(serviceIntent);
		activity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onActivityResumed(final Activity activity) {
		//Not handled
	}

	@Override
	public void onActivityPaused(final Activity activity) {
		//Not handled
	}

	@Override
	public void onActivityStopped(final Activity activity) {
		try {
			activity.unbindService(mServiceConnection);
		} catch (final IllegalArgumentException ignored) {
			//Ignore
		}
	}

	@Override
	public void onActivitySaveInstanceState(final Activity activity, final Bundle bundle) {
		//Not handled
	}

	@Override
	public void onActivityDestroyed(final Activity activity) {
		//Not handled
	}
}
