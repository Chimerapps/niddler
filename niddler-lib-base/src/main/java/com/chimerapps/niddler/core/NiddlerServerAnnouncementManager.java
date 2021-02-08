package com.chimerapps.niddler.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chimerapps.niddler.util.LogUtil;
import com.chimerapps.niddler.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
class NiddlerServerAnnouncementManager implements Runnable {

	private static final String LOG_TAG = NiddlerServerAnnouncementManager.class.getSimpleName();

	private static final int ANNOUNCEMENT_SOCKET_PORT = 6394;
	private static final int REQUEST_QUERY = 0x01;
	private static final int REQUEST_ANNOUNCE = 0x02;
	private static final int ANNOUNCEMENT_VERSION = 3;

	private static final long MAX_JOIN_WAIT = 60L;
	private static final int SLAVE_READ_TIMEOUT = 10;
	private static final int MASTER_ACCEPT_TIMEOUT = 1000;

	public static final int EXTENSION_TYPE_ICON = 1;
	public static final int EXTENSION_TYPE_TAG = 2;

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
	@NonNull
	private final List<AnnouncementExtension> mExtensions = new ArrayList<>();

	NiddlerServerAnnouncementManager(@NonNull final String packageName, @NonNull final NiddlerServer server) {
		mPackageName = packageName;
		mServer = server;
		mSlaves = new ArrayList<>();
	}

	public void addExtension(@NonNull final AnnouncementExtension extension) {
		synchronized (mExtensions) {
			mExtensions.add(extension);
		}
	}

	void start() {
		if (mIsRunning.getAndSet(true)) {
			LogUtil.niddlerLogError(LOG_TAG, "Niddler announcement server is already running!");
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
		LogUtil.niddlerLogDebug(LOG_TAG, "Starting announcement loop");

		while (mIsRunning.get()) {
			//Ensure the master or slave is closed
			closeMaster();
			closeSlave();
			try {
				//Try to become the master
				final ServerSocket masterAttempt = new ServerSocket(ANNOUNCEMENT_SOCKET_PORT);
				LogUtil.niddlerLogDebug(LOG_TAG, "Running as master");
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

		LogUtil.niddlerLogDebug(LOG_TAG, "Announcement loop finished");
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
					LogUtil.niddlerLogWarning(LOG_TAG, "Failed to accept/handle child", ignored);
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
			selfDescriptor.put("pid", -1);
			selfDescriptor.put("protocol", Niddler.NiddlerServerInfo.PROTOCOL_VERSION);

			final JSONArray extensionArray = new JSONArray();
			synchronized (mExtensions) {
				for (final AnnouncementExtension extension : mExtensions) {
					final JSONObject object = new JSONObject();
					object.put("name", extension.getName());
					object.put("data", StringUtil.toString(extension.getBytes()));
					extensionArray.put(object);
				}
			}

			selfDescriptor.put("extensions", extensionArray);
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
					slaveDescriptor.put("protocol", slave.mNiddlerProtocolVersion);

					final JSONArray extensionArray = new JSONArray();
					for (final AnnouncementExtension extension : slave.mExtensions) {
						final JSONObject object = new JSONObject();
						object.put("name", extension.getName());
						object.put("data", StringUtil.toString(extension.getBytes()));
						extensionArray.put(object);
					}

					slaveDescriptor.put("extensions", extensionArray);
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

		final int version = dataInput.readInt();

		final int packageNameLength = dataInput.readInt();
		final byte[] name = new byte[packageNameLength];
		dataInput.readFully(name);
		final int port = dataInput.readInt();
		final int pid = dataInput.readInt();
		final int niddlerProtoVersion = dataInput.readInt();

		try {
			child.setSoTimeout(SLAVE_READ_TIMEOUT);
		} catch (final IOException ignored) {
		}

		final List<AnnouncementExtension> extensions = new ArrayList<>();
		if (version == 2) {
			final int iconSize = dataInput.readInt();
			if (iconSize > 0) {
				final byte[] iconName = new byte[iconSize];
				dataInput.readFully(iconName);
				String icon = new String(iconName, "UTF-8");

				extensions.add(new IconAnnouncementExtension(icon));
			}
		} else if (version > 2) {
			final int numExtensions = dataInput.readShort();
			for (int i = 0; i < numExtensions; ++i) {
				final int type = dataInput.readShort();
				final int length = dataInput.readShort();
				final byte[] bytes = new byte[length];
				dataInput.readFully(bytes);

				switch (type) {
					case EXTENSION_TYPE_TAG:
						extensions.add(new TagAnnouncementExtension(new String(bytes, "UTF-8")));
						break;
					case EXTENSION_TYPE_ICON:
						extensions.add(new IconAnnouncementExtension(new String(bytes, "UTF-8")));
						break;
				}
			}
		}

		if (version > ANNOUNCEMENT_VERSION) {
			LogUtil.niddlerLogDebug(LOG_TAG, "Got announcement of newer version, consume all");
			int res = dataInput.read();
			while (res != -1) {
				try {
					res = dataInput.read();
				} catch (final IOException ignored) {
					break;
				}
			}
		}

		registerChild(child, dataInput, new String(name, "UTF-8"), port, pid, niddlerProtoVersion, extensions);
	}

	private void registerChild(@NonNull final Socket child,
			@NonNull final InputStream in,
			@NonNull final String packageName,
			final int port,
			final int pid,
			final int niddlerProtocolVersion,
			List<AnnouncementExtension> extensions) {
		LogUtil.niddlerLogDebug(LOG_TAG, "Got announcement for " + packageName);
		synchronized (mSlaves) {
			mSlaves.add(new Slave(child, in, packageName, port, pid, niddlerProtocolVersion, extensions));
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
			LogUtil.niddlerLogDebug(LOG_TAG, "Sending announcement for " + mPackageName);
			final DataOutputStream out = new DataOutputStream(slaveSocket.getOutputStream());
			out.write(REQUEST_ANNOUNCE);
			final byte[] packageBytes = mPackageName.getBytes("UTF-8");
			out.writeInt(ANNOUNCEMENT_VERSION);
			out.writeInt(packageBytes.length);
			out.write(packageBytes);
			out.writeInt(mServer.getPort());
			out.writeInt(-1);
			out.writeInt(Niddler.NiddlerServerInfo.PROTOCOL_VERSION);
			out.writeShort(mExtensions.size());
			for (final AnnouncementExtension extension : mExtensions) {
				out.writeShort(extension.getType());
				out.writeShort(extension.getLength());
				out.write(extension.getBytes());
			}
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
		final int mNiddlerProtocolVersion;
		@NonNull
		final List<AnnouncementExtension> mExtensions;

		Slave(@NonNull final Socket child, @NonNull final InputStream in,
				final @NonNull String packageName, final int port, final int pid,
				final int niddlerProtocolVersion,
				@NonNull final List<AnnouncementExtension> extensions) {
			mChild = child;
			mIn = in;
			mPackageName = packageName;
			mPort = port;
			mPid = pid;
			mNiddlerProtocolVersion = niddlerProtocolVersion;
			mExtensions = extensions;
		}
	}

	public static abstract class AnnouncementExtension {
		private final int mType;
		@NonNull
		private final String mName;

		protected AnnouncementExtension(final int type, @NonNull final String name) {
			mType = type;
			mName = name;
		}

		public abstract int getLength();

		@NonNull
		public abstract byte[] getBytes();

		public int getType() {
			return mType;
		}

		@NonNull
		public String getName() {
			return mName;
		}

	}

	public static abstract class StringAnnouncementExtension extends AnnouncementExtension {

		private final byte[] mData;

		public StringAnnouncementExtension(final int type, @NonNull final String name, @NonNull final String data) {
			super(type, name);
			try {
				mData = data.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int getLength() {
			return mData.length;
		}

		@NonNull
		@Override
		public byte[] getBytes() {
			return mData;
		}
	}

	public static class IconAnnouncementExtension extends StringAnnouncementExtension {

		public IconAnnouncementExtension(String icon) {
			super(EXTENSION_TYPE_ICON, "icon", icon);
		}
	}

	public static class TagAnnouncementExtension extends StringAnnouncementExtension {

		public TagAnnouncementExtension(String tag) {
			super(EXTENSION_TYPE_TAG, "tag", tag);
		}
	}
}
