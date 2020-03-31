package com.icapps.niddler.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.icapps.niddler.core.debug.NiddlerDebugger;

import java.io.IOException;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class FakeNiddlerDebugger implements NiddlerDebugger {

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isBlacklisted(@NonNull final CharSequence url) {
		return false;
	}

	@Nullable
	@Override
	public DebugRequest overrideRequest(@NonNull final NiddlerRequest request) {
		return null;
	}

	@Nullable
	@Override
	public DebugResponse handleRequest(@NonNull final NiddlerRequest request) {
		return null;
	}

	@Nullable
	@Override
	public DebugResponse handleResponse(@NonNull final NiddlerRequest request, @NonNull final NiddlerResponse response) {
		return null;
	}

	@Override
	public boolean applyDelayBeforeBlacklist() throws IOException {
		return false;
	}

	@Override
	public boolean applyDelayAfterBlacklist() throws IOException {
		return false;
	}

	@Override
	public boolean ensureCallTime(final long startTime) throws IOException {
		return false;
	}

	@Override
	public boolean waitForConnection(@NonNull final Runnable onDebuggerConnected) {
		return false;
	}

	@Override
	public boolean isWaitingForConnection() {
		return false;
	}

	@Override
	public void cancelWaitForConnection() {
	}

}
