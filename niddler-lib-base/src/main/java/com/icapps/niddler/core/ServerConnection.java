package com.icapps.niddler.core;

import androidx.annotation.Nullable;

import org.java_websocket.WebSocket;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
final class ServerConnection {

	private static final int STATE_NEW = 0;
	private static final int STATE_AUTH_REQ_SENT = 1;
	private static final int STATE_READY = 2;
	private static final int STATE_CLOSED = 3;

	private final WebSocket mSocket;
	private int mState = STATE_NEW;
	private ServerAuth.AuthRequest mAuthRequest;

	ServerConnection(final WebSocket socket) {
		mSocket = socket;
		sendProtocolInfo();
	}

	boolean canReceiveData() {
		return mState == STATE_READY;
	}

	void noAuth() {
		if (mState == STATE_NEW) {
			mState = STATE_READY;
		}
	}

	void closed() {
		mState = STATE_CLOSED;
	}

	void sendAuthRequest(@Nullable final String packageName) {
		mState = STATE_AUTH_REQ_SENT;
		mAuthRequest = ServerAuth.generateAuthenticationRequest(packageName);
		mSocket.send(MessageBuilder.buildMessage(mAuthRequest));
	}

	boolean checkAuthReply(final ServerAuth.AuthReply authReply, final String password) {
		if ((mState != STATE_AUTH_REQ_SENT) || !ServerAuth.checkAuthReply(mAuthRequest, authReply, password)) {
			mState = STATE_CLOSED;
			mSocket.close(401);
			return false;
		}
		sendAuthSuccess();
		mState = STATE_READY;
		return true;
	}

	boolean isFor(final WebSocket socket) {
		return this.mSocket == socket;
	}

	void send(final String message) {
		mSocket.send(message);
	}

	private void sendProtocolInfo() {
		mSocket.send(MessageBuilder.buildProtocolVersionMessage());
	}

	private void sendAuthSuccess() {
		mSocket.send(MessageBuilder.buildAuthSuccess());
	}

}
