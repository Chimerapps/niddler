package com.chimerapps.niddler.core;

import com.chimerapps.niddler.core.debug.NiddlerDebugger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Maarten Van Giel
 * @author Nicola Verbeeck
 */
@SuppressWarnings("WeakerAccess")
public abstract class Niddler implements Closeable {

	public static final String NIDDLER_DEBUG_RESPONSE_METADATA = "X-Niddler-Debug";
	public static final String NIDDLER_DEBUG_TIMING_RESPONSE_METADATA = "X-Niddler-Debug-Timing";
	public static final String INTENT_EXTRA_WAIT_FOR_DEBUGGER = "Niddler-Wait-For-Debugger";
	public static final String NIDDLER_FROM_DISK_METADATA = "X-Niddler-FromDiskCache";
	private static final int MAX_TRACE_CACHE_SIZE = 100;

	private final LinkedHashMap<StackTraceKey, StackTraceElement[]> mStackTraceMap;
	private final Set<StaticBlacklistListener> mBlacklistListeners;

	final NiddlerImpl mNiddlerImpl;
	private final int mStackTraceMaxDepth;

	protected Niddler(final String password,
			final int port,
			final long cacheSize,
			final NiddlerServerInfo niddlerServerInfo,
			final int stackTraceMaxDepth,
			final int pid) {
		mBlacklistListeners = new HashSet<>();
		mNiddlerImpl = new NiddlerImpl(password, port, cacheSize, niddlerServerInfo, new NiddlerImpl.StaticBlacklistDispatchListener() {

			@Override
			public void setBlacklistItemEnabled(@NonNull final String id, @NonNull final String pattern, final boolean enabled) {
				synchronized (mBlacklistListeners) {
					for (final StaticBlacklistListener blacklistListener : mBlacklistListeners) {
						if (id.equals(blacklistListener.getId())) {
							blacklistListener.setBlacklistItemEnabled(pattern, enabled);
						}
					}
				}
			}
		}, pid);
		mStackTraceMaxDepth = stackTraceMaxDepth;
		NiddlerDebuggerImpl.maxStackTraceDepth = stackTraceMaxDepth;
		mStackTraceMap = new LinkedHashMap<>();
	}

	public void logRequest(final NiddlerRequest request) {
		mNiddlerImpl.send(MessageBuilder.buildMessage(request, mStackTraceMaxDepth));
	}

	public void logResponse(final NiddlerResponse response) {
		mNiddlerImpl.send(MessageBuilder.buildMessage(response));
	}

	public boolean isStackTracingEnabled() {
		return mStackTraceMaxDepth > 0;
	}

	@Nullable
	public StackTraceElement[] popTraceForId(@NonNull final StackTraceKey stackTraceId) {
		synchronized (mStackTraceMap) {
			return mStackTraceMap.remove(stackTraceId);
		}
	}

	@NonNull
	public StackTraceKey pushStackTrace(@NonNull final StackTraceElement[] trace) {
		synchronized (mStackTraceMap) {
			final StackTraceKey traceId = new StackTraceKey(UUID.randomUUID().toString() + Arrays.hashCode(trace));
			mStackTraceMap.put(traceId, trace);
			while (mStackTraceMap.size() > MAX_TRACE_CACHE_SIZE) {
				StackTraceKey lastKey = null;
				for (Map.Entry<StackTraceKey, StackTraceElement[]> e : mStackTraceMap.entrySet()) {
					lastKey = e.getKey();
				}
				if (lastKey == null) {
					break;
				}
				mStackTraceMap.remove(lastKey);
			}
			return traceId;
		}
	}

	public void start() {
		mNiddlerImpl.start();
	}

	/**
	 * Niddler supports a single active debugger at any given time. This debugger has capabilities that add items to a blacklist, return default responses upon request, ...
	 * Interceptors should implement as many of the integrations as they can. For an example take a look at {@link com.chimerapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor}
	 *
	 * @return The debugger for niddler
	 */
	@NonNull
	public NiddlerDebugger debugger() {
		return mNiddlerImpl.debugger();
	}

	@Override
	public void close() throws IOException {
		mNiddlerImpl.close();
	}

	/**
	 * Indicates if niddler is configured to log requests, use this to determine in your interceptor if you need
	 * to generate a message
	 *
	 * @return True if this is the real niddler, in no-op mode, this returns false
	 */
	@SuppressWarnings("unused")
	public static boolean enabled() {
		return true;
	}

	/**
	 * @return True if the niddler server is started
	 */
	public boolean isStarted() {
		return mNiddlerImpl.isStarted();
	}

	/**
	 * @return True if the server is stopped
	 */
	public boolean isClosed() {
		return mNiddlerImpl.isClosed();
	}

	/**
	 * @return The socket port we are listening on
	 */
	public int getPort() {
		return mNiddlerImpl.getPort();
	}

	/**
	 * Notifies niddler that the static blacklist has been modified
	 *
	 * @param blacklist The current blacklist. Thread safe
	 */
	public void onStaticBlacklistChanged(@NonNull final String id, @NonNull final String name,
			@NonNull final List<StaticBlackListEntry> blacklist) {
		mNiddlerImpl.onStaticBlacklistChanged(id, name, blacklist);
	}

	/**
	 * Registers a listener for static blacklist updates
	 *
	 * @param listener The blacklist update listener to register
	 */
	public void registerBlacklistListener(@NonNull final StaticBlacklistListener listener) {
		synchronized (mBlacklistListeners) {
			mBlacklistListeners.add(listener);
		}
	}

	@SuppressWarnings({"WeakerAccess", "unused", "PackageVisibleField", "StaticMethodOnlyUsedInOneClass"})
	public static final class NiddlerServerInfo {

		/**
		 * Protocol version 4:
		 * - Support for configuration (blacklist, basic debugging)
		 */
		static final int PROTOCOL_VERSION = 4;
		final String name;
		final String description;
		@Nullable
		final String icon;

		/**
		 * @param name        The name to use to identify this application/session
		 * @param description A small description of this application/session/device
		 * @param icon        Optional icon to show in the UI. Can be a predefined name (eg: flutter, android, ...),
		 *                    an icon you provide in the .idea/niddler folder or a base64 encoded image
		 */
		public NiddlerServerInfo(final String name, final String description, @Nullable final String icon) {
			this.name = name;
			this.description = description;
			this.icon = icon;
		}

		/**
		 * Deprecated, use the 3 argument constructor instead
		 */
		@Deprecated
		public NiddlerServerInfo(final String name, final String description) {
			this(name, description, null);
		}

	}

	@SuppressWarnings({"unused", "SameParameterValue", "MagicNumber"})
	public static abstract class Builder<T extends Niddler> {

		protected int mPort = 6555;
		protected long mCacheSize = 1024 * 1024; // By default use 1 MB cache
		protected NiddlerServerInfo mNiddlerServerInfo = null;
		protected String mPassword;
		protected int mMaxStackTraceSize = 0;

		/**
		 * Creates a new builder with a given password to use for the niddler server authentication
		 *
		 * @param password The password to use
		 */
		public Builder(final String password) {
			mPassword = password;
		}

		/**
		 * Creates a new builder that has authentication disabled
		 */
		public Builder() {
		}

		/**
		 * Sets the port on which Niddler will listen for incoming connections
		 *
		 * @param port The port to be used
		 * @return Builder
		 */
		public Builder<T> setPort(final int port) {
			mPort = port;
			return this;
		}

		/**
		 * Sets the maximum request stack trace depth. 0 by default
		 *
		 * @param maxStackTraceSize Max stack trace depth. Set to 0 to disable request origin tracing
		 * @return Builder
		 */
		public Builder<T> setMaxStackTraceSize(int maxStackTraceSize) {
			mMaxStackTraceSize = maxStackTraceSize;
			return this;
		}

		/**
		 * Sets the cache size to be used for caching requests and responses while there is no client connected
		 *
		 * @param cacheSize The cache size to be used, in bytes
		 * @return Builder
		 */
		public Builder<T> setCacheSize(final long cacheSize) {
			mCacheSize = cacheSize;
			return this;
		}

		/**
		 * Sets additional information about this Niddler server which will be shown on the client side
		 *
		 * @param niddlerServerInfo The additional information about this Niddler server
		 * @return Builder
		 */
		public Builder<T> setNiddlerInformation(final NiddlerServerInfo niddlerServerInfo) {
			mNiddlerServerInfo = niddlerServerInfo;
			return this;
		}

		/**
		 * Builds a Niddler instance with the configured parameters
		 *
		 * @return a Niddler instance
		 */
		public abstract T build();

	}

	interface PlatformNiddler {
		void closePlatform();
	}

	public static class StackTraceKey {

		@NonNull
		private final String mId;

		StackTraceKey(@NonNull final String id) {
			mId = id;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final StackTraceKey that = (StackTraceKey) o;
			return mId.equals(that.mId);
		}

		@Override
		public int hashCode() {
			return mId.hashCode();
		}
	}

	/**
	 * Static blacklist entry
	 */
	public static class StaticBlackListEntry {
		private final Pattern mPattern;
		private boolean mEnabled;

		public StaticBlackListEntry(@NonNull final String pattern) {
			mPattern = Pattern.compile(pattern);
			mEnabled = true;
		}

		public boolean matches(@NonNull final CharSequence sequence) {
			return mEnabled && mPattern.matcher(sequence).matches();
		}

		public boolean setEnabled(final boolean value) {
			if (mEnabled != value) {
				mEnabled = value;
				return true;
			}
			return false;
		}

		public boolean isEnabled() {
			return mEnabled;
		}

		@NonNull
		public String pattern() {
			return mPattern.pattern();
		}

		public boolean isForPattern(@NonNull final String pattern) {
			return pattern.equals(mPattern.pattern());
		}
	}

	/**
	 * Listener for updating the static blacklist
	 */
	public interface StaticBlacklistListener {

		/**
		 * The id of the blacklist handler. This id must not change during the lifetime of the handler
		 *
		 * @return The id of the blacklist handler
		 */
		@NonNull
		String getId();

		/**
		 * Called when the static blacklist should be updated to reflect the new enabled status
		 *
		 * @param pattern The pattern to enable/disable
		 * @param enabled Flag indicating if the static blacklist item is enabled or disabled
		 */
		void setBlacklistItemEnabled(@NonNull final String pattern, final boolean enabled);

	}
}
