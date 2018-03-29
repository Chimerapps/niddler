package com.icapps.niddler.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class ServerAnnouncementManager implements Runnable {

	private static final int ANNOUNCEMENT_SOCKET_PORT = 6394;
	private static final int REQUEST_QUERY = 0x01;
	private static final int REQUEST_ANNOUNCE = 0x02;

	private static final String LOG_TAG = ServerAnnouncementManager.class.getSimpleName();
	private static final long MAX_JOIN_WAIT = 60L;
	private static final int SLAVE_READ_TIMEOUT = 10;
	private static final int MASTER_ACCEPT_TIMEOUT = 1000;

	private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
	@Nullable
	private volatile ServerSocket mMasterSocket;
	@Nullable
	private volatile Socket mSlaveSocket;
	@Nullable
	private Thread mThread;

	@NonNull
	private final String mPackageName;
	@NonNull
	private final NiddlerServer mServer;
	@NonNull
	private final List<Slave> mSlaves;

	ServerAnnouncementManager(@NonNull final String packageName, @NonNull final NiddlerServer server) {
		mPackageName = packageName;
		mServer = server;
		mSlaves = new ArrayList<>();
	}

	void start() {
		if (mIsRunning.getAndSet(true)) {
			Log.e(LOG_TAG, "Niddler announcement server is already running!");
			return;
		}
		final Thread thread = new Thread(this, "Niddler Announcement");
		mThread = thread;
		thread.start();
	}

	void stop() {
		mIsRunning.set(false);
		final Thread thread = mThread;
		if (thread == null) {
			return;
		}
		mThread = null;

		closeMaster();
		closeSlave();
		try {
			thread.interrupt();
			thread.join(MAX_JOIN_WAIT);
		} catch (final Throwable ignore) {
		}
	}

	@Override
	public void run() {
		Log.d(LOG_TAG, "Starting announcement loop");

		while (mIsRunning.get()) {
			//Ensure the master or slave is closed
			closeMaster();
			closeSlave();
			try {
				//Try to become the master
				final ServerSocket masterAttempt = new ServerSocket(ANNOUNCEMENT_SOCKET_PORT);
				Log.d(LOG_TAG, "Running as master");
				try {
					masterAttempt.setSoTimeout(MASTER_ACCEPT_TIMEOUT);
				} catch (final IOException e) {
					try {
						masterAttempt.close();
					} catch (final Throwable ignore) {
					}
					continue;
				}
				synchronized (this) {
					if (mIsRunning.get()) {
						mMasterSocket = masterAttempt;
					} else {
						try {
							masterAttempt.close();
						} catch (final Throwable ignore) {
						}
						break;
					}
				}

				masterLoop(masterAttempt);
			} catch (final IOException ignored) {
				if (!mIsRunning.get()) {
					break;
				}
				runSlave();
			}
		}

		Log.d(LOG_TAG, "Announcement loop finished");
	}

	private void masterLoop(@NonNull final ServerSocket masterSocket) {
		while (mIsRunning.get()) {
			try {
				final Socket child = masterSocket.accept();
				final InputStream childInput = child.getInputStream();
				final int command = childInput.read();
				if (command == REQUEST_QUERY) {
					handleQuery(child);
				} else if (command == REQUEST_ANNOUNCE) {
					handleAnnounce(child, childInput);
				}
			} catch (final SocketTimeoutException timeout) {
				if (mIsRunning.get()) {
					reapSlaves();
				}
			} catch (final Throwable ignored) {
				if (mIsRunning.get()) {
					Log.w(LOG_TAG, "Failed to accept/handle child", ignored);
				}
			}
		}
		synchronized (mSlaves) {
			for (final Slave slave : mSlaves) {
				try {
					slave.mChild.close();
				} catch (final IOException ignored) {
				}
			}
			mSlaves.clear();
		}
	}

	private void handleQuery(@NonNull final Socket child) throws IOException {
		final JSONArray responseArray = new JSONArray();

		final JSONObject selfDescriptor = new JSONObject();
		try {
			selfDescriptor.put("packageName", mPackageName);
			selfDescriptor.put("port", mServer.getPort());
			selfDescriptor.put("pid", android.os.Process.myPid());
		} catch (final JSONException ignored) {
		}
		responseArray.put(selfDescriptor);

		synchronized (mSlaves) {
			for (final Slave slave : mSlaves) {
				final JSONObject slaveDescriptor = new JSONObject();
				try {
					slaveDescriptor.put("packageName", slave.mPackageName);
					slaveDescriptor.put("port", slave.mPort);
					slaveDescriptor.put("pid", slave.mPid);
				} catch (final JSONException ignored) {
				}
				responseArray.put(slaveDescriptor);
			}
		}

		final String response = responseArray.toString();
		final OutputStream out = child.getOutputStream();
		out.write(response.getBytes("UTF-8"));
		out.write('\n');
		out.flush();
		child.close();
	}

	private void handleAnnounce(@NonNull final Socket child, @NonNull final InputStream childInput) throws IOException {
		final DataInputStream dataInput = new DataInputStream(childInput);
		final int packageNameLength = dataInput.readInt();
		final byte[] name = new byte[packageNameLength];
		dataInput.readFully(name);
		final int port = dataInput.readInt();
		final int pid = dataInput.readInt();

		registerChild(child, dataInput, new String(name, "UTF-8"), port, pid);
	}

	private void registerChild(@NonNull final Socket child, @NonNull final InputStream in, @NonNull final String packageName, final int port, final int pid) {
		try {
			child.setSoTimeout(SLAVE_READ_TIMEOUT);
		} catch (final IOException ignored) {
		}
		Log.d(LOG_TAG, "Got announcement for " + packageName);
		synchronized (mSlaves) {
			mSlaves.add(new Slave(child, in, packageName, port, pid));
		}
	}

	private void reapSlaves() {
		synchronized (mSlaves) {
			final Iterator<Slave> it = mSlaves.iterator();
			while (it.hasNext()) {
				try {
					if (it.next().mIn.read() == -1) {
						it.remove();
					}
				} catch (final SocketTimeoutException ignored) {
					//Timeout is fine, we only want to check if the channel is still alive
				} catch (final IOException ignored) {
					it.remove();
				}
			}
		}
	}

	private void runSlave() {
		try {
			final Socket slaveSocket = new Socket(InetAddress.getByName("127.0.0.1"), ANNOUNCEMENT_SOCKET_PORT);
			synchronized (this) {
				if (mIsRunning.get()) {
					mSlaveSocket = slaveSocket;
				} else {
					slaveSocket.close();
				}
			}
			Log.d(LOG_TAG, "Sending announcement for " + mPackageName);
			final DataOutputStream out = new DataOutputStream(slaveSocket.getOutputStream());
			out.write(REQUEST_ANNOUNCE);
			final byte[] packageBytes = mPackageName.getBytes("UTF-8");
			out.writeInt(packageBytes.length);
			out.write(packageBytes);
			out.writeInt(mServer.getPort());
			out.writeInt(android.os.Process.myPid());
			out.flush();

			//noinspection ResultOfMethodCallIgnored
			slaveSocket.getInputStream().read();
			closeSlave();
		} catch (final IOException ignored) {
		}
	}

	private void closeMaster() {
		synchronized (this) {
			final ServerSocket master = mMasterSocket;
			if (master != null) {
				try {
					master.close();
				} catch (final Throwable ignore) {
				}
				mMasterSocket = null;
			}
		}
	}

	private void closeSlave() {
		synchronized (this) {
			final Socket slave = mSlaveSocket;
			if (slave != null) {
				try {
					slave.close();
				} catch (final Throwable ignore) {
				}
				mSlaveSocket = null;
			}
		}
	}

	private static class Slave {

		@NonNull
		final Socket mChild;
		@NonNull
		final InputStream mIn;
		@NonNull
		final String mPackageName;
		final int mPort;
		final int mPid;

		Slave(@NonNull final Socket child, @NonNull final InputStream in, final @NonNull String packageName, final int port, final int pid) {
			mChild = child;
			mIn = in;
			mPackageName = packageName;
			mPort = port;
			mPid = pid;
		}
	}
}
