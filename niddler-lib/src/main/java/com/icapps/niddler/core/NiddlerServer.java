package com.icapps.niddler.core;

import android.util.Log;
import com.icapps.niddler.util.Logging;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
class NiddlerServer extends WebSocketServer {

	private static final String LOG_TAG = NiddlerServer.class.getSimpleName();
	private final WebSocketListener mListener;

	private NiddlerServer(final InetSocketAddress address, final WebSocketListener listener) {
		super(address);
		mListener = listener;
	}

	NiddlerServer(final int port, final WebSocketListener listener) throws UnknownHostException {
		this(new InetSocketAddress(port), listener);
	}

	@Override
	public final void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "New socket connection: " + handshake.getResourceDescriptor());
		}

		if (mListener != null) {
			mListener.onConnectionOpened(conn);
		}
	}

	@Override
	public final void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, "Connection closed: " + conn);
		}
	}

	@Override
	public final void onMessage(final WebSocket conn, final String message) {
		if (Logging.DO_LOG) {
			Log.d(LOG_TAG, conn + ": " + message);
		}

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
		final Collection<WebSocket> connections = connections();
		for (final WebSocket socket : connections) {
			try {
				socket.send(message);
			} catch (final NotYetConnectedException ignored) {
				//Nothing to do, wait for the connection to complete
			} catch (final IllegalArgumentException ignored) {
				Log.e(LOG_TAG, "WebSocket error", ignored);
			}
		}
	}

	interface WebSocketListener {
		void onConnectionOpened(final WebSocket conn);
	}

}
