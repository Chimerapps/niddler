package com.icapps.niddler.core;

import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.icapps.niddler.core.debug.NiddlerDebugger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import static com.icapps.niddler.core.NiddlerDebuggerImpl.DebugAction.extractId;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
final class NiddlerDebuggerImpl implements NiddlerDebugger {

	private static final String TAG = "NiddlerDebugger";

	private static final String DEBUG_TYPE_KEY = "controlType";
	private static final String DEBUG_PAYLOAD = "payload";
	private static final String KEY_MESSAGE_ID = "messageId";

	private static final String MESSAGE_ACTIVATE = "activate";
	private static final String MESSAGE_DEACTIVATE = "deactivate";
	private static final String MESSAGE_MUTE_ACTIONS = "muteActions";
	private static final String MESSAGE_UNMUTE_ACTIONS = "unmuteActions";
	private static final String MESSAGE_ADD_BLACKLIST = "addBlacklist";
	private static final String MESSAGE_REMOVE_BLACKLIST = "removeBlacklist";
	private static final String MESSAGE_ADD_DEFAULT_RESPONSE = "addDefaultResponse";
	private static final String MESSAGE_DEBUG_REPLY = "debugReply";
	private static final String MESSAGE_ADD_REQUEST = "addRequest";
	private static final String MESSAGE_REMOVE_REQUEST = "removeRequest";
	private static final String MESSAGE_ADD_RESPONSE = "addResponse";
	private static final String MESSAGE_REMOVE_RESPONSE = "removeResponse";
	private static final String MESSAGE_ACTIVATE_ACTION = "activateAction";
	private static final String MESSAGE_DEACTIVATE_ACTION = "deactivateAction";
	private static final String MESSAGE_DELAYS = "updateDelays";
	private static final String MESSAGE_ADD_DEFAULT_REQUEST_OVERRIDE = "addDefaultRequestOverride";
	private static final String MESSAGE_ADD_REQUEST_OVERRIDE = "addRequestOverride";
	private static final String MESSAGE_REMOVE_REQUEST_OVERRIDE = "removeRequestOverride";

	@NonNull
	private final DebuggerConfiguration mDebuggerConfiguration;
	@Nullable
	private transient ServerConnection mServerConnection;
	@NonNull
	private final Map<String, CompletableFuture<DebugResponse>> mWaitingResponses;
	@NonNull
	private final Map<String, CompletableFuture<DebugRequest>> mWaitingRequests;

	@Nullable
	private Runnable mDebuggerConnectionListener;

	NiddlerDebuggerImpl() {
		mDebuggerConfiguration = new DebuggerConfiguration();
		mWaitingResponses = new HashMap<>();
		mWaitingRequests = new HashMap<>();
	}

	void onDebuggerAttached(@NonNull final ServerConnection connection) {
		onDebuggerConnectionClosed();
		mServerConnection = connection;
	}

	void onDebuggerConnectionClosed() {
		mDebuggerConfiguration.connectionLost();
		mServerConnection = null;
		synchronized (mWaitingResponses) {
			for (final Map.Entry<String, CompletableFuture<DebugResponse>> entry : mWaitingResponses.entrySet()) {
				entry.getValue().offer(null);
			}
			mWaitingResponses.clear();
		}
		synchronized (mWaitingRequests) {
			for (final Map.Entry<String, CompletableFuture<DebugRequest>> entry : mWaitingRequests.entrySet()) {
				entry.getValue().offer(null);
			}
			mWaitingRequests.clear();
		}
	}

	private void onDebuggerConfigurationMessage(@NonNull final String messageType, final JSONObject body) {
		try {
			switch (messageType) {
				case MESSAGE_ACTIVATE:
					mDebuggerConfiguration.setActive(true);
					synchronized (mDebuggerConfiguration) {
						if (mDebuggerConfiguration.isWaitingForDebugger()) {
							mDebuggerConfiguration.setWaitingForDebugger(false);
							if (mDebuggerConnectionListener != null) {
								mDebuggerConnectionListener.run();
							}
							mDebuggerConnectionListener = null;
						}
					}
					break;
				case MESSAGE_DEACTIVATE:
					mDebuggerConfiguration.setActive(false);
					break;
				case MESSAGE_MUTE_ACTIONS:
					mDebuggerConfiguration.muteActions(true);
					break;
				case MESSAGE_UNMUTE_ACTIONS:
					mDebuggerConfiguration.muteActions(false);
					break;
				case MESSAGE_ADD_BLACKLIST:
					mDebuggerConfiguration.addBlacklist(body.getString("regex"));
					break;
				case MESSAGE_REMOVE_BLACKLIST:
					mDebuggerConfiguration.removeBlacklist(body.getString("regex"));
					break;
				case MESSAGE_ADD_DEFAULT_RESPONSE:
					mDebuggerConfiguration.addRequestAction(new DefaultResponseAction(body));
					break;
				case MESSAGE_DEBUG_REPLY:
					onDebugResponse(body.getString(KEY_MESSAGE_ID), parseResponse(body));
					break;
				case MESSAGE_ADD_REQUEST:
					mDebuggerConfiguration.addRequestAction(new DebugRequestAction(body));
					break;
				case MESSAGE_REMOVE_REQUEST:
					mDebuggerConfiguration.removeRequestAction(extractId(body));
					break;
				case MESSAGE_ADD_RESPONSE:
					mDebuggerConfiguration.addResponseAction(new DebugRequestResponseAction(body));
					break;
				case MESSAGE_REMOVE_RESPONSE:
					mDebuggerConfiguration.removeResponseAction(extractId(body));
					break;
				case MESSAGE_ACTIVATE_ACTION:
					mDebuggerConfiguration.setActionActive(extractId(body), true);
					break;
				case MESSAGE_DEACTIVATE_ACTION:
					mDebuggerConfiguration.setActionActive(extractId(body), false);
					break;
				case MESSAGE_DELAYS:
					mDebuggerConfiguration.updateDelays(body.optLong("preBlacklist"), body.optLong("postBlacklist"), body.optLong("timePerCall"));
					break;
				case MESSAGE_ADD_DEFAULT_REQUEST_OVERRIDE:
					mDebuggerConfiguration.addRequestOverrideAction(new DefaultRequestOverrideAction(body));
					break;
				case MESSAGE_REMOVE_REQUEST_OVERRIDE:
					mDebuggerConfiguration.removeRequestOverrideAction(extractId(body));
					break;
				case MESSAGE_ADD_REQUEST_OVERRIDE:
					mDebuggerConfiguration.addRequestOverrideAction(new DebugRequestOverrideAction(body));
					break;
			}
		} catch (final JSONException e) {
			if (Log.isLoggable(TAG, Log.WARN)) {
				Log.w(TAG, "Invalid debugger json message:", e);
			}
		}
	}

	@Override
	public boolean isActive() {
		return mDebuggerConfiguration.active();
	}

	@Override
	public boolean isBlacklisted(@NonNull final CharSequence url) {
		return mDebuggerConfiguration.inBlacklisted(url);
	}

	@Nullable
	@Override
	public DebugRequest overrideRequest(@NonNull final NiddlerRequest request) {
		final ServerConnection conn = mServerConnection;
		if (conn == null) {
			return null;
		}
		try {
			final CompletableFuture<DebugRequest> future = mDebuggerConfiguration.handleRequestOverride(request, this);
			if (future == null) {
				return null;
			}
			return future.get();
		} catch (final Throwable ignored) {
			return null;
		}
	}

	@Nullable
	@Override
	public DebugResponse handleRequest(@NonNull final NiddlerRequest request) {
		final ServerConnection conn = mServerConnection;
		if (conn == null) {
			return null;
		}
		try {
			final CompletableFuture<DebugResponse> future = mDebuggerConfiguration.handleRequest(request, this);
			if (future == null) {
				return null;
			}
			return future.get();
		} catch (final Throwable ignored) {
			return null;
		}
	}

	@Nullable
	@Override
	public DebugResponse handleResponse(@NonNull final NiddlerRequest request, @NonNull final NiddlerResponse response) {
		final ServerConnection connection = mServerConnection;
		if (connection == null) {
			return null;
		}
		try {
			final CompletableFuture<DebugResponse> future = mDebuggerConfiguration.handleResponse(request, response, this);
			if (future == null) {
				return null;
			}
			return future.get();
		} catch (final Throwable ignored) {
			return null;
		}
	}

	@Override
	public boolean applyDelayBeforeBlacklist() throws IOException {
		final long timeout = mDebuggerConfiguration.preBlacklistTimeout();
		if (timeout <= 0L) {
			return false;
		}
		try {
			Thread.sleep(timeout);
			return true;
		} catch (final InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean applyDelayAfterBlacklist() throws IOException {
		final long timeout = mDebuggerConfiguration.postBlacklistTimeout();
		if (timeout <= 0L) {
			return false;
		}
		try {
			Thread.sleep(timeout);
			return true;
		} catch (final InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean ensureCallTime(final long startTime) throws IOException {
		final long totalTimeInFlight = (System.nanoTime() - startTime) / 1000L;
		final long minDuration = mDebuggerConfiguration.minimalCallDuration();
		final long diff = minDuration - totalTimeInFlight;
		if (diff <= 0) {
			return false;
		}

		try {
			Thread.sleep(diff);
			return true;
		} catch (final InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean waitForConnection(@NonNull final Runnable onDebuggerConnected) {
		synchronized (mDebuggerConfiguration) {
			if (mServerConnection != null && mDebuggerConfiguration.active()) {
				return false;
			}
			if (mDebuggerConfiguration.isWaitingForDebugger()) {
				return false;
			}

			mDebuggerConfiguration.setWaitingForDebugger(true);
			mDebuggerConnectionListener = onDebuggerConnected;
			return true;
		}
	}

	@Override
	public boolean isWaitingForConnection() {
		synchronized (mDebuggerConfiguration) {
			return mDebuggerConfiguration.isWaitingForDebugger();
		}
	}

	@Override
	public void cancelWaitForConnection() {
		synchronized (mDebuggerConfiguration) {
			mDebuggerConnectionListener = null;
			mDebuggerConfiguration.setWaitingForDebugger(false);
		}
	}

	void onControlMessage(@NonNull final JSONObject object, final ServerConnection connection) throws JSONException {
		if (mServerConnection != connection) {
			return;
		}

		onDebuggerConfigurationMessage(object.getString(DEBUG_TYPE_KEY), object.optJSONObject(DEBUG_PAYLOAD));
	}

	@Nullable
	CompletableFuture<DebugResponse> sendHandleRequest(@NonNull final NiddlerRequest request, @Nullable final NiddlerResponse response) {
		final ServerConnection connection = mServerConnection;
		if (connection == null) {
			return null;
		}
		return DebuggerConfiguration.sendHandleResponse(request, response, connection, mWaitingResponses);
	}

	@Nullable
	CompletableFuture<DebugRequest> sendHandleRequestOverride(@NonNull final NiddlerRequest request) {
		final ServerConnection connection = mServerConnection;
		if (connection == null) {
			return null;
		}
		return DebuggerConfiguration.sendHandleRequestOverride(request, connection, mWaitingRequests);
	}

	private void onDebugResponse(@NonNull final String messageId, @NonNull final DebugResponse response) {
		final CompletableFuture<DebugResponse> future;
		synchronized (mWaitingResponses) {
			future = mWaitingResponses.get(messageId);
			if (future != null) {
				mWaitingResponses.remove(messageId);
			}
		}
		if (future != null) {
			future.offer(response);
		}
	}

	void onConnectionClosed(final ServerConnection connection) {
		if (mServerConnection == connection) {
			onDebuggerConnectionClosed();
		}
	}

	static final class DebuggerConfiguration {

		private final ReentrantReadWriteLock mReadWriteLock = new ReentrantReadWriteLock(false);
		private final Lock mWriteLock = mReadWriteLock.writeLock();
		private final Lock mReadLock = mReadWriteLock.readLock();

		private final List<Pattern> mBlacklist = new ArrayList<>();
		private final List<RequestOverrideAction> mRequestOverrideActions = new ArrayList<>();
		private final List<RequestAction> mRequestActions = new ArrayList<>();
		private final List<ResponseAction> mResponseActions = new ArrayList<>();
		private boolean mIsActive = false;
		private boolean mActionsMuted = false;
		private long mPreBlacklistTimeout = 0L;
		private long mPostBlacklistTimeout = 0L;
		private long mTimePerCall = 0L;
		private boolean mWaitingForDebugger;
		private final ConditionVariable mWaitingForDebuggerVariable = new ConditionVariable();

		DebuggerConfiguration() {
			mWaitingForDebuggerVariable.open();
		}

		boolean active() {
			try {
				mReadLock.lock();
				return mIsActive;
			} finally {
				mReadLock.unlock();
			}
		}

		boolean isWaitingForDebugger() {
			try {
				mReadLock.lock();
				return mWaitingForDebugger;
			} finally {
				mReadLock.unlock();
			}
		}

		void setWaitingForDebugger(boolean waiting) {
			try {
				mWriteLock.lock();
				mWaitingForDebugger = waiting;
				if (mWaitingForDebugger) {
					mWaitingForDebuggerVariable.close();
				} else {
					mWaitingForDebuggerVariable.open();
				}
			} finally {
				mWriteLock.unlock();
			}
		}

		void muteActions(final boolean muted) {
			mWriteLock.lock();
			mActionsMuted = muted;
			mWriteLock.unlock();
		}

		void connectionLost() {
			try {
				mWriteLock.lock();

				mBlacklist.clear();
				mRequestActions.clear();
				mResponseActions.clear();
				mRequestOverrideActions.clear();
				mIsActive = false;
			} finally {
				mWriteLock.unlock();
			}
		}

		void addBlacklist(@NonNull final String regex) {
			mWriteLock.lock();
			if (mIsActive) {
				mBlacklist.add(Pattern.compile(regex));
			}
			mWriteLock.unlock();
		}

		void removeBlacklist(@NonNull final String regex) {
			mWriteLock.lock();
			final Iterator<Pattern> it = mBlacklist.iterator();
			while (it.hasNext()) {
				if (it.next().pattern().equals(regex)) {
					it.remove();
				}
			}
			mWriteLock.unlock();
		}

		boolean inBlacklisted(@NonNull final CharSequence url) {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();

				if (mIsActive) {
					for (final Pattern pattern : mBlacklist) {
						if (pattern.matcher(url).matches()) {
							return true;
						}
					}
				}
				return false;
			} finally {
				mReadLock.unlock();
			}
		}

		void addRequestAction(@NonNull final RequestAction action) {
			mWriteLock.lock();
			mRequestActions.add(action);
			mWriteLock.unlock();
		}

		void removeRequestAction(@NonNull final String actionId) {
			mWriteLock.lock();

			final Iterator<RequestAction> it = mRequestActions.iterator();
			while (it.hasNext()) {
				if (it.next().id.equals(actionId)) {
					it.remove();
				}
			}

			mWriteLock.unlock();
		}

		void addResponseAction(@NonNull final ResponseAction action) {
			mWriteLock.lock();
			mResponseActions.add(action);
			mWriteLock.unlock();
		}

		void removeResponseAction(@NonNull final String actionId) {
			mWriteLock.lock();

			final Iterator<ResponseAction> it = mResponseActions.iterator();
			while (it.hasNext()) {
				if (it.next().id.equals(actionId)) {
					it.remove();
				}
			}

			mWriteLock.unlock();
		}

		void addRequestOverrideAction(@NonNull final RequestOverrideAction action) {
			mWriteLock.lock();
			mRequestOverrideActions.add(action);
			mWriteLock.unlock();
		}

		void removeRequestOverrideAction(@NonNull final String actionId) {
			mWriteLock.lock();

			final Iterator<RequestOverrideAction> it = mRequestOverrideActions.iterator();
			while (it.hasNext()) {
				if (it.next().id.equals(actionId)) {
					it.remove();
				}
			}

			mWriteLock.unlock();
		}

		void setActionActive(@NonNull final String actionId, final boolean isActive) {
			mWriteLock.lock();

			for (final ResponseAction responseAction : mResponseActions) {
				if (responseAction.id.equals(actionId)) {
					responseAction.active = isActive;
				}
			}
			for (final RequestAction requestAction : mRequestActions) {
				if (requestAction.id.equals(actionId)) {
					requestAction.active = isActive;
				}
			}

			mWriteLock.unlock();
		}

		@Nullable
		CompletableFuture<DebugRequest> handleRequestOverride(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) throws IOException {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();

				if (mIsActive && !mActionsMuted) {
					for (final RequestOverrideAction requestOverrideAction : mRequestOverrideActions) {
						final CompletableFuture<DebugRequest> response = requestOverrideAction.handleRequestOverride(request, debugger);
						if (response != null) {
							return response;
						}
					}
				}

				return null;
			} finally {
				mReadLock.unlock();
			}
		}

		@Nullable
		CompletableFuture<DebugResponse> handleRequest(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) throws IOException {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();

				if (mIsActive && !mActionsMuted) {
					for (final RequestAction requestAction : mRequestActions) {
						final CompletableFuture<DebugResponse> response = requestAction.handleRequest(request, debugger);
						if (response != null) {
							return response;
						}
					}
				}

				return null;
			} finally {
				mReadLock.unlock();
			}
		}

		@Nullable
		CompletableFuture<DebugResponse> handleResponse(@NonNull final NiddlerRequest request,
				@NonNull final NiddlerResponse response,
				final NiddlerDebuggerImpl niddlerDebugger) throws IOException {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();

				if (mIsActive && !mActionsMuted) {
					for (final ResponseAction responseAction : mResponseActions) {
						final CompletableFuture<DebugResponse> serverResponse = responseAction.handleResponse(request, response, niddlerDebugger);
						if (serverResponse != null) {
							return serverResponse;
						}
					}
				}

				return null;
			} finally {
				mReadLock.unlock();
			}
		}

		@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
		static CompletableFuture<DebugResponse> sendHandleResponse(@NonNull final NiddlerRequest request,
				@Nullable final NiddlerResponse response,
				@NonNull final ServerConnection connection,
				@NonNull final Map<String, CompletableFuture<DebugResponse>> waitingResponses) {
			final CompletableFuture<DebugResponse> future = new CompletableFuture<>();

			synchronized (waitingResponses) {
				waitingResponses.put(request.getMessageId(), future);
			}
			try {
				connection.send(makeDebugRequestMessage(request, response));
			} catch (final JSONException e) {
				future.offer(null);
				if (Log.isLoggable(TAG, Log.WARN)) {
					Log.w(TAG, "Failed to offer:", e);
				}
			}
			return future;
		}

		@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
		static CompletableFuture<DebugRequest> sendHandleRequestOverride(@NonNull final NiddlerRequest request,
				@NonNull final ServerConnection connection,
				@NonNull final Map<String, CompletableFuture<DebugRequest>> waitingRequests) {
			final CompletableFuture<DebugRequest> future = new CompletableFuture<>();

			synchronized (waitingRequests) {
				waitingRequests.put(request.getMessageId(), future);
			}
			try {
				connection.send(makeDebugRequestOverrideMessage(request));
			} catch (final JSONException e) {
				future.offer(null);
				if (Log.isLoggable(TAG, Log.WARN)) {
					Log.w(TAG, "Failed to offer:", e);
				}
			}
			return future;
		}

		long preBlacklistTimeout() {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();
				return mIsActive ? mPreBlacklistTimeout : 0L;
			} finally {
				mReadLock.unlock();
			}
		}

		long postBlacklistTimeout() {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();
				return mIsActive ? mPostBlacklistTimeout : 0L;
			} finally {
				mReadLock.unlock();
			}
		}

		long minimalCallDuration() {
			mWaitingForDebuggerVariable.block();
			try {
				mReadLock.lock();
				return mIsActive ? mTimePerCall : 0L;
			} finally {
				mReadLock.unlock();
			}
		}

		void updateDelays(@Nullable final Long preBlacklist, @Nullable final Long postBlacklist, @Nullable final Long timePerCall) {
			try {
				mWriteLock.lock();
				mPreBlacklistTimeout = preBlacklist == null ? 0L : preBlacklist;
				mPostBlacklistTimeout = postBlacklist == null ? 0L : postBlacklist;
				mTimePerCall = timePerCall == null ? 0L : timePerCall;
			} finally {
				mWriteLock.unlock();
			}
		}

		void setActive(final boolean active) {
			try {
				mWriteLock.lock();
				mIsActive = active;
			} finally {
				mWriteLock.unlock();
			}
		}
	}

	static abstract class DebugAction {
		@NonNull
		final String id;
		final int repeatCount;
		final AtomicInteger callCount;

		transient boolean active;

		DebugAction(@NonNull final String id, final boolean active, final int repeatCount) {
			this.id = id;
			this.active = active;
			this.repeatCount = repeatCount;
			callCount = new AtomicInteger(0);
		}

		@NonNull
		static String extractId(@NonNull final JSONObject object) throws JSONException {
			return object.getString("id");
		}

		static boolean extractActiveState(@NonNull final JSONObject object) {
			return object.optBoolean("active", true);
		}

		static int extractRepeatCount(@NonNull final JSONObject object) {
			return object.optInt("repeatCount", -1);
		}

		@Nullable
		static String extractMatchingRegex(final JSONObject object) {
			return object.optString("regex");
		}

	}

	static abstract class RequestOverrideAction extends DebugAction {

		RequestOverrideAction(@NonNull final String id, final boolean active, final int repeatCount) {
			super(id, active, repeatCount);
		}

		@Nullable
		abstract CompletableFuture<DebugRequest> handleRequestOverride(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) throws IOException;
	}

	static abstract class RequestAction extends DebugAction {

		RequestAction(@NonNull final String id, final boolean active, final int repeatCount) {
			super(id, active, repeatCount);
		}

		@Nullable
		abstract CompletableFuture<DebugResponse> handleRequest(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) throws IOException;

	}

	static abstract class ResponseAction extends DebugAction {

		ResponseAction(@NonNull final String id, final boolean active, final int repeatCount) {
			super(id, active, repeatCount);
		}

		@Nullable
		abstract CompletableFuture<DebugResponse> handleResponse(@NonNull final NiddlerRequest request,
				@NonNull final NiddlerResponse response,
				@NonNull final NiddlerDebuggerImpl debugger) throws IOException;

	}

	static final class DefaultResponseAction extends RequestAction {

		@Nullable
		private final Pattern mRegex;
		@Nullable
		private final String mMethod;

		@NonNull
		private final DebugResponse mDebugResponse;

		DefaultResponseAction(final JSONObject object) throws JSONException {
			super(extractId(object), extractActiveState(object), extractRepeatCount(object));

			final String regexString = extractMatchingRegex(object);

			mRegex = regexString == null ? null : Pattern.compile(regexString);
			mMethod = object.optString("matchMethod");
			mDebugResponse = parseResponse(object);
		}

		@Nullable
		@Override
		CompletableFuture<DebugResponse> handleRequest(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) {
			if (!active) {
				return null;
			}

			if (mRegex != null && !mRegex.matcher(request.getUrl()).matches()) {
				return null;
			}
			if (mMethod != null && !mMethod.equalsIgnoreCase(request.getMethod())) {
				return null;
			}
			if (repeatCount > 0 && callCount.incrementAndGet() >= repeatCount) {
				return null;
			}

			return new CompletableFuture<>(mDebugResponse);
		}
	}

	static final class DebugRequestAction extends RequestAction {
		@Nullable
		private final Pattern mRegex;
		@Nullable
		private final String mMethod;

		DebugRequestAction(final JSONObject object) throws JSONException {
			super(extractId(object), extractActiveState(object), extractRepeatCount(object));

			final String regexString = extractMatchingRegex(object);

			mRegex = regexString == null ? null : Pattern.compile(regexString);
			mMethod = object.optString("matchMethod");
		}

		@Nullable
		@Override
		CompletableFuture<DebugResponse> handleRequest(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) {
			if (!active) {
				return null;
			}

			if (mRegex != null && !mRegex.matcher(request.getUrl()).matches()) {
				return null;
			}
			if (mMethod != null && !mMethod.equalsIgnoreCase(request.getMethod())) {
				return null;
			}
			if (repeatCount > 0 && callCount.incrementAndGet() >= repeatCount) {
				return null;
			}

			return debugger.sendHandleRequest(request, null);
		}
	}

	static final class DebugRequestResponseAction extends ResponseAction {
		@Nullable
		private final Pattern mRegex;
		@Nullable
		private final String mMethod;
		@Nullable
		private final Integer mResponseCode;

		DebugRequestResponseAction(final JSONObject object) throws JSONException {
			super(extractId(object), extractActiveState(object), extractRepeatCount(object));

			final String regexString = extractMatchingRegex(object);

			mRegex = regexString == null ? null : Pattern.compile(regexString);
			mMethod = object.optString("matchMethod");
			mResponseCode = object.has("responseCode") ? object.optInt("responseCode") : null;
		}

		@Nullable
		@Override
		CompletableFuture<DebugResponse> handleResponse(@NonNull final NiddlerRequest request,
				@NonNull final NiddlerResponse response,
				@NonNull final NiddlerDebuggerImpl debugger) {
			if (!active) {
				return null;
			}

			if (mRegex != null && !mRegex.matcher(request.getUrl()).matches()) {
				return null;
			}
			if (mMethod != null && !mMethod.equalsIgnoreCase(request.getMethod())) {
				return null;
			}
			if (mResponseCode != null) {
				final Integer code = response.getStatusCode();
				if (code != null && ((int) mResponseCode) != code) {
					return null;
				}
			}
			if (repeatCount > 0 && callCount.incrementAndGet() >= repeatCount) {
				return null;
			}

			return debugger.sendHandleRequest(request, response);
		}
	}

	static final class DefaultRequestOverrideAction extends RequestOverrideAction {
		@Nullable
		private final Pattern mRegex;
		@Nullable
		private final String mMethod;
		@NonNull
		private final DebugRequest mDebugRequest;

		DefaultRequestOverrideAction(final JSONObject object) throws JSONException {
			super(extractId(object), extractActiveState(object), extractRepeatCount(object));

			final String regexString = extractMatchingRegex(object);

			mRegex = regexString == null ? null : Pattern.compile(regexString);
			mMethod = object.optString("matchMethod");
			mDebugRequest = parseResponseOverride(object);
		}

		@Nullable
		@Override
		CompletableFuture<DebugRequest> handleRequestOverride(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) {
			if (!active) {
				return null;
			}

			if (mRegex != null && !mRegex.matcher(request.getUrl()).matches()) {
				return null;
			}
			if (mMethod != null && !mMethod.equalsIgnoreCase(request.getMethod())) {
				return null;
			}
			if (repeatCount > 0 && callCount.incrementAndGet() >= repeatCount) {
				return null;
			}

			return new CompletableFuture<>(mDebugRequest);
		}
	}

	static final class DebugRequestOverrideAction extends RequestOverrideAction {
		@Nullable
		private final Pattern mRegex;
		@Nullable
		private final String mMethod;

		DebugRequestOverrideAction(final JSONObject object) throws JSONException {
			super(extractId(object), extractActiveState(object), extractRepeatCount(object));

			final String regexString = extractMatchingRegex(object);

			mRegex = regexString == null ? null : Pattern.compile(regexString);
			mMethod = object.optString("matchMethod");
		}

		@Nullable
		@Override
		CompletableFuture<DebugRequest> handleRequestOverride(@NonNull final NiddlerRequest request, @NonNull final NiddlerDebuggerImpl debugger) {
			if (!active) {
				return null;
			}

			if (mRegex != null && !mRegex.matcher(request.getUrl()).matches()) {
				return null;
			}
			if (mMethod != null && !mMethod.equalsIgnoreCase(request.getMethod())) {
				return null;
			}
			if (repeatCount > 0 && callCount.incrementAndGet() >= repeatCount) {
				return null;
			}

			return debugger.sendHandleRequestOverride(request);
		}
	}

	@NonNull
	static DebugResponse parseResponse(@NonNull final JSONObject config) throws JSONException {
		return new DebugResponse(config.getInt("code"),
				config.getString("message"),
				parseHeaders(config.optJSONObject("headers")),
				config.optString("encodedBody"),
				config.optString("bodyMimeType"));
	}

	@NonNull
	static DebugRequest parseResponseOverride(@NonNull final JSONObject config) throws JSONException {
		return new DebugRequest(config.getString("url"),
				config.getString("method"),
				parseHeaders(config.optJSONObject("headers")),
				config.optString("encodedBody"),
				config.optString("bodyMimeType"));
	}

	@Nullable
	private static Map<String, List<String>> parseHeaders(@Nullable final JSONObject headersObject) throws JSONException {
		if (headersObject == null) {
			return null;
		}

		final Map<String, List<String>> headers = new HashMap<>();

		final Iterator<String> keys = headersObject.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			final JSONArray array = headersObject.getJSONArray(key);
			final int numItems = array.length();
			final List<String> headersForKey = new ArrayList<>(numItems);
			for (int i = 0; i < numItems; ++i) {
				headersForKey.add(array.getString(i));
			}
			headers.put(key, headersForKey);
		}

		return headers;
	}

	@NonNull
	static String makeDebugRequestMessage(@NonNull final NiddlerRequest request, @Nullable final NiddlerResponse response) throws JSONException {
		final JSONObject object = new JSONObject();
		object.put("type", "debugRequest");
		object.put("requestId", request.getMessageId());
		if (response != null) {
			object.put("response", MessageBuilder.buildMessageJson(response));
		}
		return object.toString();
	}

	@NonNull
	static String makeDebugRequestOverrideMessage(@NonNull final NiddlerRequest request) throws JSONException {
		final JSONObject requestObj = MessageBuilder.buildMessageJson(request);

		final JSONObject object = new JSONObject();
		object.put("type", "debugRequest");
		object.put("request", requestObj);

		return object.toString();
	}

	static class CompletableFuture<T> implements Future<T> {

		private final BlockingQueue<OptionalWrapper<T>> reply = new ArrayBlockingQueue<>(1);
		private volatile boolean done = false;

		CompletableFuture() {
		}

		CompletableFuture(T value) {
			try {
				reply.put(new OptionalWrapper<>(value));
			} catch (final InterruptedException ignored) {
			}
		}

		void offer(final T value) {
			try {
				done = true;
				reply.put(new OptionalWrapper<T>(value));
			} catch (final InterruptedException ignored) {
				if (Log.isLoggable(TAG, Log.WARN)) {
					Log.w(TAG, "Failed to offer:", ignored);
				}
			}
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return done;
		}

		@Override
		public T get() throws InterruptedException {
			return reply.take().value;
		}

		@Override
		public T get(final long timeout, @NonNull final TimeUnit unit) throws InterruptedException, TimeoutException {
			final OptionalWrapper<T> replyOrNull = reply.poll(timeout, unit);
			if (replyOrNull == null) {
				throw new TimeoutException();
			}
			return replyOrNull.value;
		}
	}

	static class OptionalWrapper<T> {

		T value;

		OptionalWrapper(T value) {
			this.value = value;
		}
	}

}
