package com.icapps.niddler.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.icapps.niddler.service.NiddlerService;

import java.lang.ref.WeakReference;

import static com.icapps.niddler.core.Niddler.INTENT_EXTRA_WAIT_FOR_DEBUGGER;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
class NiddlerServiceLifeCycleWatcher implements Application.ActivityLifecycleCallbacks {

	private static final String WAIT_FOR_DEBUGGER_FRAGMENT_ID = "Niddler-Wait-For-Debugger";

	@NonNull
	private final ServiceConnection mServiceConnection;
	@NonNull
	private final Niddler mNiddler;

	NiddlerServiceLifeCycleWatcher(@NonNull final ServiceConnection connection, @NonNull final Niddler niddler) {
		mServiceConnection = connection;
		mNiddler = niddler;
	}

	@Override
	public void onActivityCreated(final Activity activity, final Bundle bundle) {
		final int waitForDebugger = activity.getIntent().getIntExtra(INTENT_EXTRA_WAIT_FOR_DEBUGGER, 0);
		if (waitForDebugger == 1) {

			final WeakReference<Activity> weakActivity = new WeakReference<>(activity);

			if (mNiddler.debugger().waitForConnection(new Runnable() {
				@Override
				public void run() {
					final Activity startingActivity = weakActivity.get();
					if (startingActivity != null) {
						startingActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final Fragment frag = startingActivity.getFragmentManager().findFragmentByTag(WAIT_FOR_DEBUGGER_FRAGMENT_ID);
								if (frag != null) {
									startingActivity.getFragmentManager().beginTransaction().remove(frag).commitAllowingStateLoss();
								}
							}
						});
					}
				}
			})) {
				final AlertDialogFragment fragment = new AlertDialogFragment();
				fragment.setRetainInstance(true);
				fragment.mNiddler = mNiddler;
				activity.getFragmentManager().beginTransaction().add(fragment, WAIT_FOR_DEBUGGER_FRAGMENT_ID).commitAllowingStateLoss();
			}
		}
	}

	@Override
	public void onActivityStarted(final Activity activity) {
		final Intent serviceIntent = new Intent(activity, NiddlerService.class);
		try {
			activity.startService(serviceIntent);
			activity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		} catch(final Throwable ignore) {
		}
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
		} catch (final Throwable ignored) {
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

	public static class AlertDialogFragment extends DialogFragment {

		public Niddler mNiddler;

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage("Waiting for niddler debugger to attach")
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							dialog.cancel();
						}
					})
					.create();
		}

		@Override
		public void onCancel(final DialogInterface dialog) {
			super.onCancel(dialog);
			if (mNiddler != null) {
				mNiddler.debugger().cancelWaitForConnection();
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			if (mNiddler != null) {
				if (!mNiddler.debugger().isWaitingForConnection()) {
					dismissAllowingStateLoss();
				}
			}
		}
	}
}
