package com.chimerapps.niddler.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chimerapps.niddler.core.debug.NiddlerDebugger;
import com.chimerapps.niddler.util.LogUtil;
import com.chimerapps.niddler.util.StringUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
class NiddlerServer extends WebSocketServer {

	private static final String LOG_TAG = NiddlerServer.class.getSimpleName();

	private final String mPackageName;
	private final WebSocketListener mListener;
	private final List<ServerConnection> mConnections;
	private final String mPassword;
	private final NiddlerDebuggerImpl mNiddlerDebugger;
	private final NiddlerServerAnnouncementManager mServerAnnouncementManager;
	private final NiddlerImpl.StaticBlacklistDispatchListener mStaticBlacklistListener;
	private final String mTag;

	private NiddlerServer(final String password, final InetSocketAddress address, final String packageName, @Nullable final String icon,
			final WebSocketListener listener, final NiddlerImpl.StaticBlacklistDispatchListener blacklistListener) {
		super(address);
		mPackageName = packageName;
		mListener = listener;
		mPassword = password;
		mConnections = new LinkedList<>();
		mNiddlerDebugger = new NiddlerDebuggerImpl();
		mServerAnnouncementManager = new NiddlerServerAnnouncementManager(packageName, this);
		mStaticBlacklistListener = blacklistListener;
		mTag = UUID.randomUUID().toString().substring(0, 6);

		if (icon != null) {
			mServerAnnouncementManager.addExtension(new NiddlerServerAnnouncementManager.IconAnnouncementExtension(icon));
		}
		mServerAnnouncementManager.addExtension(new NiddlerServerAnnouncementManager.TagAnnouncementExtension(mTag));
	}

	NiddlerServer(final String password, final int port, final String packageName, @Nullable final String icon,
			final WebSocketListener listener, final NiddlerImpl.StaticBlacklistDispatchListener blacklistListener) throws UnknownHostException {
		this(password, new InetSocketAddress(port), packageName, icon, listener, blacklistListener);
	}

	@Override
	public void start() {
		mServerAnnouncementManager.stop();
		super.start();
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		mServerAnnouncementManager.stop();
		super.stop();
	}

	@Override
	public final void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		LogUtil.niddlerLogDebug(LOG_TAG, "New socket connection: " + handshake.getResourceDescriptor());

		final ServerConnection connection = new ServerConnection(conn);
		synchronized (mConnections) {
			mConnections.add(connection);
		}
		if (StringUtil.isEmpty(mPassword)) {
			connection.noAuth();
			authSuccess(conn);
		} else {
			connection.sendAuthRequest(mPackageName);
		}
	}

	@Override
	public final void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		LogUtil.niddlerLogDebug(LOG_TAG, "Connection closed: " + conn);

		synchronized (mConnections) {
			final Iterator<ServerConnection> iterator = mConnections.iterator();
			while (iterator.hasNext()) {
				final ServerConnection connection = iterator.next();
				mNiddlerDebugger.onConnectionClosed(connection);
				connection.closed();
				if (connection.isFor(conn)) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public void onStart() {
		mServerAnnouncementManager.stop();
		mServerAnnouncementManager.start();
		LogUtil.niddlerLogStartup("Niddler Server running on " + getPort() + " [" + mTag + "][waitingForDebugger=" + mNiddlerDebugger.isWaitingForConnection() + "]");
	}

	private static final String MESSAGE_AUTH = "authReply";
	private static final String MESSAGE_START_DEBUG = "startDebug";
	private static final String MESSAGE_END_DEBUG = "endDebug";
	private static final String MESSAGE_DEBUG_CONTROL = "controlDebug";
	private static final String MESSAGE_STATIC_BLACKLIST_UPDATE = "controlStaticBlacklist";

	@Override
	public final void onMessage(final WebSocket conn, final String message) {
		final ServerConnection connection = getConnection(conn);
		if (connection == null) {
			conn.close();
			return;
		}

		try {
			final JSONObject object = new JSONObject(message);
			final String type = object.optString("type", null);
			switch (type) {
				case MESSAGE_AUTH:
					if (!connection.checkAuthReply(MessageParser.parseAuthReply(object), mPassword)) {
						LogUtil.niddlerLogWarning(LOG_TAG, "Client sent wrong authentication code!");
						return;
					}
					authSuccess(conn);
					break;
				case MESSAGE_START_DEBUG:
					if (connection.canReceiveData()) {
						mNiddlerDebugger.onDebuggerAttached(connection);
					}
					break;
				case MESSAGE_END_DEBUG:
					mNiddlerDebugger.onDebuggerConnectionClosed();
					break;
				case MESSAGE_DEBUG_CONTROL:
					mNiddlerDebugger.onControlMessage(object, connection);
					break;
				case MESSAGE_STATIC_BLACKLIST_UPDATE:
					mStaticBlacklistListener.setBlacklistItemEnabled(object.getString("id"), object.getString("pattern"), object.getBoolean("enabled"));
					break;
				default:
					LogUtil.niddlerLogWarning(LOG_TAG, "Received unsolicited message from client: " + message);
			}
		} catch (final JSONException e) {
			LogUtil.niddlerLogWarning(LOG_TAG, "Received non-json message from server: " + message, e);
		}
	}

	private ServerConnection getConnection(final WebSocket conn) {
		synchronized (mConnections) {
			for (final ServerConnection connection : mConnections) {
				if (connection.isFor(conn)) {
					return connection;
				}
			}
		}
		return null;
	}

	private void authSuccess(final WebSocket conn) {
		if (mListener != null) {
			mListener.onConnectionOpened(conn);
		}
	}

	@Override
	public final void onError(final WebSocket conn, final Exception ex) {
		LogUtil.niddlerLogError(LOG_TAG, "WebSocket error", ex);

		final ServerConnection connection = getConnection(conn);
		if (connection != null) {
			mNiddlerDebugger.onConnectionClosed(connection);
			connection.closed();
		}
	}

	/**
	 * Sends a String message to all sockets
	 *
	 * @param message the message to be sent
	 */
	final synchronized void sendToAll(final String message) {
		synchronized (mConnections) {
			for (final ServerConnection connection : mConnections) {
				try {
					if (connection.canReceiveData()) {
						connection.send(message);
					}
				} catch (final NotYetConnectedException ignored) {
					//Nothing to do, wait for the connection to complete
				} catch (final IllegalArgumentException ignored) {
					LogUtil.niddlerLogError(LOG_TAG, "WebSocket error", ignored);
				}
			}
		}
	}

	@NonNull
	NiddlerDebugger debugger() {
		return mNiddlerDebugger;
	}

	interface WebSocketListener {
		void onConnectionOpened(final WebSocket conn);
	}

}
