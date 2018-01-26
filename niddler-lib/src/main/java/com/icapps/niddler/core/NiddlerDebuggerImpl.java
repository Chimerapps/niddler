package com.icapps.niddler.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.icapps.niddler.core.debug.NiddlerDebugger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	private static final String MESSAGE_MUTE_ACTIONS = "muteActions";
	private static final String MESSAGE_UNMUTE_ACTIONS = "unmuteActions";
	private static final String MESSAGE_ADD_BLACKLIST = "addBlacklist";
	private static final String MESSAGE_REMOVE_BLACKLIST = "removeBlacklist";
	private static final String MESSAGE_ADD_DEFAULT_RESPONSE = "addDefaultResponse";
	private static final String MESSAGE_REMOVE_REQUEST_ACTION = "removeRequestAction";

	@NonNull
	private final DebuggerConfiguration mDebuggerConfiguration;
	@Nullable
	private transient ServerConnection mServerConnection;

	NiddlerDebuggerImpl() {
		mDebuggerConfiguration = new DebuggerConfiguration();
	}

	void onDebuggerAttached(@NonNull final ServerConnection connection) {
		mDebuggerConfiguration.connectionLost();
		mServerConnection = connection;
	}

	void onDebuggerConnectionClosed() {
		mDebuggerConfiguration.connectionLost();
		mServerConnection = null;
	}

	void onDebuggerConfigurationMessage(@NonNull final String messageType, final JSONObject body) {
		try {
			switch (messageType) {
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
				case MESSAGE_REMOVE_REQUEST_ACTION:
					mDebuggerConfiguration.removeRequestAction(extractId(body));
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
	public DebugResponse handleRequest(@NonNull final NiddlerRequest request) {
		final ServerConnection conn = mServerConnection;
		if (conn == null) {
			return null;
		}
		try {
			return mDebuggerConfiguration.handleRequest(request, conn);
		} catch (final IOException ignored) {
			return null;
		}
	}

	@Nullable
	@Override
	public DebugResponse handleResponse(@NonNull final NiddlerRequest request, @NonNull final NiddlerResponse response) {
		return null; //TODO
	}

	static final class DebuggerConfiguration {

		private final ReentrantReadWriteLock mReadWriteLock = new ReentrantReadWriteLock(false);
		private final Lock mWriteLock = mReadWriteLock.writeLock();
		private final Lock mReadLock = mReadWriteLock.readLock();

		private List<Pattern> mBlacklist = new ArrayList<>();
		private List<RequestAction> mRequestActions = new ArrayList<>();
		private boolean mIsActive = false;
		private boolean mActionsMuted = false;

		boolean active() {
			mReadLock.lock();
			boolean isActive = mIsActive;
			mReadLock.unlock();
			return isActive;
		}

		void muteActions(final boolean muted) {
			mWriteLock.lock();
			mActionsMuted = muted;
			mWriteLock.unlock();
		}

		void connectionLost() {
			mWriteLock.lock();
			mBlacklist.clear();
			mRequestActions.clear();
			mIsActive = false;
			mWriteLock.unlock();
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
			mReadLock.lock();

			if (mIsActive) {
				for (final Pattern pattern : mBlacklist) {
					if (pattern.matcher(url).matches()) {
						mReadLock.unlock();
						return true;
					}
				}
			}

			mReadLock.unlock();
			return false;
		}

		void addRequestAction(@NonNull final RequestAction action) {
			mWriteLock.lock();
			if (mIsActive) {
				mRequestActions.add(action);
			}
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

		@Nullable
		DebugResponse handleRequest(@NonNull final NiddlerRequest request, @NonNull final ServerConnection connection) throws IOException {
			try {
				mReadLock.lock();

				if (mIsActive && !mActionsMuted) {
					for (final RequestAction requestAction : mRequestActions) {
						final DebugResponse response = requestAction.handleRequest(request, connection);
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
	}

	static abstract class DebugAction {
		@NonNull
		final String id;

		DebugAction(@NonNull final String id) {
			this.id = id;
		}

		@NonNull
		static String extractId(final JSONObject object) throws JSONException {
			return object.getString("id");
		}

		@Nullable
		static String extractMatchingRegex(final JSONObject object) {
			return object.optString("regex");
		}

		@NonNull
		static <T> T notNull(final T data) throws JSONException {
			if (data == null) {
				throw new JSONException("Item was expected to be non-null");
			}
			return data;
		}

	}

	static abstract class RequestAction extends DebugAction {

		RequestAction(@NonNull final String id) {
			super(id);
		}

		abstract DebugResponse handleRequest(@NonNull final NiddlerRequest request, @NonNull final ServerConnection connection) throws IOException;

	}

	static final class DefaultResponseAction extends RequestAction {

		@NonNull
		private final Pattern mRegex;
		@NonNull
		private final DebugResponse mDebugResponse;

		DefaultResponseAction(final JSONObject object) throws JSONException {
			super(extractId(object));

			mRegex = Pattern.compile(notNull(extractMatchingRegex(object)));
			mDebugResponse = parseDefaultResponse(object);
		}

		@Override
		DebugResponse handleRequest(@NonNull final NiddlerRequest request, @NonNull final ServerConnection connection) {
			if (mRegex.matcher(request.getUrl()).matches()) {
				return mDebugResponse;
			}
			return null;
		}
	}

	@NonNull
	static DebugResponse parseDefaultResponse(@NonNull final JSONObject config) {
		try {
			return new DebugResponse(config.getInt("code"),
					config.getString("message"),
					parseHeaders(config.optJSONObject("headers")),
					config.optString("encodedBody"),
					config.optString("bodyMimeType"));
		} catch (final JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	@Nullable
	private static Map<String, String> parseHeaders(@Nullable final JSONObject headersObject) throws JSONException {
		if (headersObject == null) {
			return null;
		}

		final Map<String, String> headers = new HashMap<>();

		final Iterator<String> keys = headersObject.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			final String value = headersObject.getString(key);
			headers.put(key, value);
		}

		return headers;
	}

}
