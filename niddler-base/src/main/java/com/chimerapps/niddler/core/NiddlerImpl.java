package com.chimerapps.niddler.core;

import androidx.annotation.NonNull;

import com.chimerapps.niddler.core.debug.NiddlerDebugger;
import com.chimerapps.niddler.util.LogUtil;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class NiddlerImpl implements NiddlerServer.WebSocketListener {

	private static final String LOG_TAG = Niddler.class.getSimpleName();

	private NiddlerServer mServer;
	private Niddler.PlatformNiddler mPlatform;

	private final MessagesCache mMessageCache;
	private final Niddler.NiddlerServerInfo mNiddlerServerInfo;

	private boolean mIsStarted = false;
	private boolean mIsClosed = false;
	@NonNull
	private final Map<String, String> mStaticBlacklistMessage;


	NiddlerImpl(final String password, final int port, final long cacheSize, final Niddler.NiddlerServerInfo niddlerServerInfo,
			final StaticBlacklistDispatchListener blacklistListener) {
		try {
			mServer = new NiddlerServer(password, port, niddlerServerInfo.name, niddlerServerInfo.icon, this, blacklistListener);
		} catch (final UnknownHostException ex) {
			LogUtil.niddlerLogError(LOG_TAG, "Failed to start server: " + ex.getLocalizedMessage());
		}
		mMessageCache = new MessagesCache(cacheSize);
		mNiddlerServerInfo = niddlerServerInfo;
		mStaticBlacklistMessage = new HashMap<>();
	}

	@Override
	public void onConnectionOpened(final WebSocket conn) {
		if (mNiddlerServerInfo != null) {
			conn.send(MessageBuilder.buildMessage(mNiddlerServerInfo));
		}
		for (final String message : mMessageCache.get()) {
			conn.send(message);
		}
		if (!mStaticBlacklistMessage.isEmpty()) {
			for (final Map.Entry<String, String> entry : mStaticBlacklistMessage.entrySet()) {
				conn.send(entry.getValue());
			}
		}
	}

	void start() {
		if ((mServer != null) && !mIsStarted) {
			mServer.start();
			mIsStarted = true;
			LogUtil.niddlerLogDebug(LOG_TAG, "Started niddler server on " + mServer.getAddress());
		}
	}

	void setPlatform(final Niddler.PlatformNiddler platform) {
		mPlatform = platform;
	}

	void close() throws IOException {
		final Niddler.PlatformNiddler platform = mPlatform;
		if (platform != null) {
			platform.closePlatform();
		}

		if (mServer != null) {
			try {
				mServer.stop();
			} catch (final InterruptedException e) {
				throw new IOException(e);
			} finally {
				mIsClosed = true;
				mMessageCache.clear();
			}
		}
	}

	NiddlerDebugger debugger() {
		return mServer.debugger();
	}

	boolean isStarted() {
		return mIsStarted;
	}

	boolean isClosed() {
		return mIsClosed;
	}

	void send(final String message) {
		if (mServer != null) {
			mMessageCache.put(message);
			mServer.sendToAll(message);
		}
	}

	void onStaticBlacklistChanged(@NonNull final String id, @NonNull final String name,
			@NonNull final List<Niddler.StaticBlackListEntry> blacklist) {
		final String message = MessageBuilder.buildMessage(id, name, blacklist);
		mStaticBlacklistMessage.put(id, message);
		if (isStarted() && !isClosed() && !mStaticBlacklistMessage.isEmpty()) {
			mServer.sendToAll(message);
		}

	}

	int getPort() {
		return mServer.getPort();
	}

	interface StaticBlacklistDispatchListener {

		/**
		 * Called when the static blacklist should be updated to reflect the new enabled status
		 *
		 * @param pattern The pattern to enable/disable
		 * @param enabled Flag indicating if the static blacklist item is enabled or disabled
		 * @param id      The id of the blacklist handler to update
		 */
		void setBlacklistItemEnabled(@NonNull final String id, @NonNull final String pattern, final boolean enabled);

	}
}
