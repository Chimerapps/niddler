package com.icapps.niddler.core;

import android.util.Log;
import com.icapps.niddler.util.Logging;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
class NiddlerServer extends WebSocketServer {

	private static final String LOG_TAG = NiddlerServer.class.getSimpleName();
	private final WebSocketListener mListener;
	private final List<ServerConnection> mConnections;
	private final String mPassword;

	private NiddlerServer(final String password, final InetSocketAddress address, final WebSocketListener listener) {
		super(address);
		mListener = listener;
		mPassword = password;
		mConnections = new LinkedList<>();
	}

	NiddlerServer(final String password, final int port, final WebSocketListener listener) throws UnknownHostException {
		this(password, new InetSocketAddress(port), listener);
	}

	@Override
	public final void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "New socket connection: " + handshake.getResourceDescriptor());
		}
		final ServerConnection connection = new ServerConnection(conn);
		synchronized (mConnections) {
			mConnections.add(connection);
		}
		connection.sendAuthRequest();
	}

	@Override
	public final void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "Connection closed: " + conn);
		}
		synchronized (mConnections) {
			final Iterator<ServerConnection> iterator = mConnections.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().isFor(conn)) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public final void onMessage(final WebSocket conn, final String message) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, conn + ": " + message);
		}
		final ServerConnection connection = getConnection(conn);
		if (connection == null) {
			conn.close();
			return;
		}

		try {
			final JSONObject object = new JSONObject(message);
			final String type = object.optString("type");
			switch (type) {
				case "authReply":
					if (!connection.checkAuthReply(MessageParser.parseAuthReply(object), mPassword)) {
						if (Logging.DO_LOG) {
							Log.w(LOG_TAG, "Client sent wrong authentication code!");
						}
					}
					if (mListener != null) {
						mListener.onConnectionOpened(conn);
					}
					break;
				default:
					if (Logging.DO_LOG) {
						Log.w(LOG_TAG, "Received unsolicited message from client: " + message);
					}
			}
		} catch (final JSONException e) {
			if (Logging.DO_LOG) {
				Log.w(LOG_TAG, "Received non-json message from server: " + message, e);
			}
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

	@Override
	public final void onError(final WebSocket conn, final Exception ex) {
		if (Logging.DO_LOG) {
			Log.e(LOG_TAG, "WebSocket error", ex);
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
					Log.e(LOG_TAG, "WebSocket error", ignored);
				}
			}
		}
	}

	interface WebSocketListener {
		void onConnectionOpened(final WebSocket conn);
	}

}
