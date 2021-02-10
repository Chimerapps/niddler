package com.chimerapps.niddler.urlconnection;

import com.chimerapps.niddler.core.Niddler;
import com.chimerapps.niddler.core.debug.NiddlerDebugger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Helper class for installing niddler as the URL handler for http and https connections
 *
 * @author Nicola Verbeeck
 */
public final class NiddlerUrlConnectionHandler {

	@Nullable
	private final Niddler niddler;
	@NonNull
	private final List<Niddler.StaticBlackListEntry> blacklist;
	@NonNull
	private final String id = UUID.randomUUID().toString();
	@Nullable
	private final NiddlerDebugger debugger;

	private NiddlerUrlConnectionHandler(@Nullable final Niddler niddler) {
		this.niddler = niddler;
		blacklist = new CopyOnWriteArrayList<>();
		if (niddler != null) {
			debugger = niddler.debugger();
			niddler.registerBlacklistListener(new Niddler.StaticBlacklistListener() {

				@NonNull
				@Override
				public String getId() {
					return id;
				}

				@Override
				public void setBlacklistItemEnabled(@NonNull final String pattern, final boolean enabled) {
					NiddlerUrlConnectionHandler.this.setBlacklistItemEnabled(pattern, enabled);
				}
			});
		} else {
			debugger = null;
		}
	}

	/**
	 * Registers niddler to the URL handlers for HTTP and HTTPS. This method only works correctly when it is used before any other factories have been set for HTTP and HTTPs.
	 * <p>
	 * Note that this method uses some reflection tricks to get the default delegates, if this fails, we use quasi-recursion
	 * Note: If niddler is in no-op mode, this method does nothing
	 *
	 * @param niddler The niddler instance to use
	 * @return An instance of the connection handler that allows some configuration to be update
	 */
	@NonNull
	public static NiddlerUrlConnectionHandler install(@NonNull final Niddler niddler) {
		if (!Niddler.enabled()) {
			return new NiddlerUrlConnectionHandler(null);
		}
		final NiddlerUrlConnectionHandler connectionHandler = new NiddlerUrlConnectionHandler(niddler);
		if (!(installWithDelegatesFromURL(niddler, connectionHandler) || installWithDelegatesFromAndroid(niddler, connectionHandler))) {
			installUsingFactoryHacking(niddler, connectionHandler);
		}

		return connectionHandler;

	}

	/**
	 * Registers niddler to the URL handlers for HTTP and HTTPS. This convenience method allows you to specify the handlers for http and https that will be used.
	 * <p>
	 * Note: If niddler is in no-op mode, this method does nothing
	 *
	 * @param niddler      The niddler instance to use
	 * @param httpHandler  The URLStreamHandler for http connections
	 * @param httpsHandler The URLStreamHandler for https connections
	 * @return An instance of the connection handler that allows some configuration to be update
	 */
	public static NiddlerUrlConnectionHandler install(@NonNull final Niddler niddler, @NonNull final URLStreamHandler httpHandler, @NonNull final URLStreamHandler httpsHandler) {
		if (!Niddler.enabled()) {
			return new NiddlerUrlConnectionHandler(null);
		}
		final NiddlerUrlConnectionHandler connectionHandler = new NiddlerUrlConnectionHandler(niddler);
		installFactoryWithDelegates(niddler, httpHandler, httpsHandler, connectionHandler);
		return connectionHandler;
	}

	/**
	 * Adds a static blacklist on the given url pattern. The pattern is interpreted as a java regex ({@link Pattern}). Items matching the blacklist are not tracked by niddler.
	 * This blacklist is independent from any debugger blacklists
	 *
	 * @param urlPattern The pattern to add to the blacklist
	 * @return This instance
	 */
	@NonNull
	public NiddlerUrlConnectionHandler blacklist(@NonNull final String urlPattern) {
		if (niddler != null) {
			blacklist.add(new Niddler.StaticBlackListEntry(urlPattern));
			niddler.onStaticBlacklistChanged(id, "URLConnection", blacklist);
		}
		return this;
	}

	boolean isBlacklisted(@NonNull final CharSequence url) {
		for (final Niddler.StaticBlackListEntry entry : blacklist) {
			if (entry.matches(url)) {
				return true;
			}
		}
		if (debugger != null) {
			return debugger.isBlacklisted(url);
		}
		return false;
	}

	/**
	 * Allows you to enable/disable static blacklist items based on the pattern. This only affects the static blacklist, independent from debugger blacklists
	 *
	 * @param pattern The pattern to enable/disable in the blacklist. If a pattern is added that does not exist yet in the blacklist, it is added
	 * @param enabled Flag indicating if the static blacklist item should be enabled or disabled
	 */
	private void setBlacklistItemEnabled(@NonNull final String pattern, final boolean enabled) {
		if (niddler == null) {
			return;
		}
		boolean modified = false;
		for (final Niddler.StaticBlackListEntry blackListEntry : blacklist) {
			if (blackListEntry.isForPattern(pattern)) {
				if (blackListEntry.setEnabled(enabled)) {
					modified = true;
				}
			}
		}
		if (!modified) {
			final Niddler.StaticBlackListEntry entry = new Niddler.StaticBlackListEntry(pattern);
			entry.setEnabled(enabled);
			blacklist.add(entry);
		}
		niddler.onStaticBlacklistChanged(id, "URLConnection", blacklist);
	}

	@SuppressWarnings("PrivateApi")
	private static boolean installWithDelegatesFromAndroid(@NonNull final Niddler niddler, @NonNull final NiddlerUrlConnectionHandler handler) {
		try {
			final URLStreamHandler httpDelegate = (URLStreamHandler) Class.forName("com.android.okhttp.HttpHandler").newInstance();
			final URLStreamHandler httpsDelegate = (URLStreamHandler) Class.forName("com.android.okhttp.HttpsHandler").newInstance();

			installFactoryWithDelegates(niddler, httpDelegate, httpsDelegate, handler);

			return true;
		} catch (final Throwable ignored) {
			//Ignore
			return false;
		}
	}

	private static boolean installWithDelegatesFromURL(@NonNull final Niddler niddler, @NonNull final NiddlerUrlConnectionHandler handler) {
		try {
			final Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
			method.setAccessible(true);
			final URLStreamHandler httpDelegate = (URLStreamHandler) method.invoke(null, "http");
			final URLStreamHandler httpsDelegate = (URLStreamHandler) method.invoke(null, "https");

			installFactoryWithDelegates(niddler, httpDelegate, httpsDelegate, handler);
			return true;
		} catch (final Throwable ignored) {
			//Ignore
			return false;
		}
	}

	private static void installFactoryWithDelegates(@NonNull final Niddler niddler,
			@NonNull final URLStreamHandler httpDelegate,
			@NonNull final URLStreamHandler httpsDelegate,
			@NonNull final NiddlerUrlConnectionHandler handler) {
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(final String protocol) {
				if ("http".equals(protocol)) {
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(final URL url) throws IOException {
							return new DelegatingHttpUrlConnection(url, URLStreamHandlerHelper.openConnection(httpDelegate, url), niddler, handler);
						}

						@Override
						protected URLConnection openConnection(final URL url, final Proxy proxy) throws IOException {
							return new DelegatingHttpUrlConnection(url, URLStreamHandlerHelper.openConnection(httpDelegate, url, proxy), niddler, handler);
						}
					};
				} else if ("https".equals(protocol)) {
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(final URL url) throws IOException {
							return new DelegatingHttpsUrlConnection(url, (HttpsURLConnection) URLStreamHandlerHelper.openConnection(httpsDelegate, url), niddler, handler);
						}

						@Override
						protected URLConnection openConnection(final URL url, final Proxy proxy) throws IOException {
							return new DelegatingHttpsUrlConnection(url, (HttpsURLConnection) URLStreamHandlerHelper.openConnection(httpsDelegate, url, proxy), niddler, handler);
						}
					};
				}
				return null;
			}
		});
	}

	private static void installUsingFactoryHacking(@NonNull final Niddler niddler, @NonNull final NiddlerUrlConnectionHandler handler) {
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(final String protocol) {
				if ("http".equals(protocol) || "https".equals(protocol)) {
					final URLStreamHandlerFactory parent = this;
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(final URL url) throws IOException {
							synchronized (parent) {
								try {
									URL.setURLStreamHandlerFactory(null);

									if ("http".equals(protocol)) {
										return new DelegatingHttpUrlConnection(url, niddler, handler);
									} else {
										return new DelegatingHttpsUrlConnection(url, niddler, handler);
									}
								} finally {
									URL.setURLStreamHandlerFactory(parent);
								}
							}
						}

						@Override
						protected URLConnection openConnection(final URL url, final Proxy proxy) throws IOException {
							synchronized (parent) {
								try {
									URL.setURLStreamHandlerFactory(null);

									if ("http".equals(protocol)) {
										return new DelegatingHttpUrlConnection(url, proxy, niddler, handler);
									} else {
										return new DelegatingHttpsUrlConnection(url, proxy, niddler, handler);
									}
								} finally {
									URL.setURLStreamHandlerFactory(parent);
								}
							}
						}
					};
				}
				return null;
			}
		});
	}
}
