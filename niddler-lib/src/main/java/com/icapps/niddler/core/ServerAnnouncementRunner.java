package com.icapps.niddler.core;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ServerAnnouncementRunner implements Runnable {

	private static final int ANNOUNCEMENT_PACKET_SIZE = 20;
	private static final byte[] ANNOUNCEMENT_PACKET = {'N', 'I', 'D', 'D', 'L', 'E', 'R', '-',
			'A', 'N', 'N', 'O', 'U', 'N', 'C', 'E', 'M', 'E', 'N', 'T'};
	private static final String LOG_TAG = ServerAnnouncementRunner.class.getSimpleName();

	@NonNull
	private final DatagramSocket mSocket;
	@NonNull
	private final byte[] mPackageNameBuffer;
	@NonNull
	private final NiddlerServer mNiddlerServer;

	ServerAnnouncementRunner(@NonNull final DatagramSocket socket,
			@NonNull final String packageName,
			@NonNull final NiddlerServer niddlerServer) {
		mSocket = socket;
		mPackageNameBuffer = packageName.getBytes();
		mNiddlerServer = niddlerServer;
	}

	@Override
	public void run() {
		Log.d(LOG_TAG, "Starting announcement loop");
		final DatagramPacket packet = new DatagramPacket(new byte[ANNOUNCEMENT_PACKET_SIZE], ANNOUNCEMENT_PACKET_SIZE);
		while (mSocket.isBound()) {
			try {
				mSocket.receive(packet);
				handlePacket(packet);
			} catch (IOException e) {
				if (mSocket.isClosed() || !mSocket.isBound()) {
					break;
				}
			}
		}
		Log.d(LOG_TAG, "Announcement loop finished");
	}

	private void handlePacket(@NonNull final DatagramPacket packet) {
		final byte[] data = packet.getData();
		if (data.length != ANNOUNCEMENT_PACKET.length) {
			return;
		}

		if (!Arrays.equals(data, ANNOUNCEMENT_PACKET)) {
			return;
		}

		final byte[] portString = String.valueOf(mNiddlerServer.getPort()).getBytes();
		//Package name + ';' + port + 0
		final byte[] reply = new byte[mPackageNameBuffer.length + portString.length + 2];
		System.arraycopy(mPackageNameBuffer, 0, reply, 0, mPackageNameBuffer.length);
		reply[mPackageNameBuffer.length] = ';';
		System.arraycopy(portString, 0, reply, mPackageNameBuffer.length + 1, portString.length);
		reply[reply.length - 1] = 0;

		final InetAddress sender = packet.getAddress();
		final DatagramPacket responsePacket = new DatagramPacket(reply, reply.length);
		responsePacket.setAddress(sender);
		try {
			mSocket.send(responsePacket);
		} catch (IOException e) {
			Log.d(LOG_TAG, "Failed to response with announcement packet", e);
		}
	}

}
