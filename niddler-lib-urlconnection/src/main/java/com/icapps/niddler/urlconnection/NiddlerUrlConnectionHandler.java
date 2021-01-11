package com.icapps.niddler.urlconnection;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 */
public final class NiddlerUrlConnectionHandler {

	//Utility class
	private NiddlerUrlConnectionHandler() {
	}

	/**
	 * Registers niddler to the URL handlers for HTTP and HTTPS. This method only works correctly when it is used before any other factories have been set for HTTP and HTTPs.
	 * <p>
	 * Note that this method uses some reflection tricks to get the default delegates.
	 *
	 * @param niddler The niddler instance to use
	 * @throws IOException When installing fails
	 */
	public static void install(@NonNull final Niddler niddler) throws IOException {
		if (installWithDelegatesFromURL(niddler)) {
			return;
		} else if (installWithDelegatesFromAndroid(niddler)) {
			return;
		} else if (installUsingFactoryHacking(niddler)) {
			return;
		}

		throw new IOException("Failed to find any valid alternatives to install");
	}

	private static boolean installWithDelegatesFromAndroid(@NonNull final Niddler niddler) {
		try {
			final URLStreamHandler httpDelegate = (URLStreamHandler) Class.forName("com.android.okhttp.HttpHandler").newInstance();
			final URLStreamHandler httpsDelegate = (URLStreamHandler) Class.forName("com.android.okhttp.HttpsHandler").newInstance();

			installFactoryWithDelegates(niddler, httpDelegate, httpsDelegate);

			return true;
		} catch (final Throwable ignored) {
			//Ignore
			return false;
		}
	}

	private static boolean installWithDelegatesFromURL(@NonNull final Niddler niddler) {
		try {
			final Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
			method.setAccessible(true);
			final URLStreamHandler httpDelegate = (URLStreamHandler) method.invoke(null, "http");
			final URLStreamHandler httpsDelegate = (URLStreamHandler) method.invoke(null, "https");

			installFactoryWithDelegates(niddler, httpDelegate, httpsDelegate);
			return true;
		} catch (final Throwable ignored) {
			//Ignore
			return false;
		}
	}

	private static void installFactoryWithDelegates(@NonNull final Niddler niddler, final URLStreamHandler httpDelegate, final URLStreamHandler httpsDelegate) {
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(final String protocol) {
				if ("http".equals(protocol)) {
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(final URL url) throws IOException {
							return new DelegatingHttpUrlConnection(url, URLStreamHandlerHelper.openConnection(httpDelegate, url), niddler);
						}

						@Override
						protected URLConnection openConnection(final URL url, final Proxy proxy) throws IOException {
							return new DelegatingHttpUrlConnection(url, URLStreamHandlerHelper.openConnection(httpDelegate, url, proxy), niddler);
						}
					};
				} else if ("https".equals(protocol)) {
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(final URL url) throws IOException {
							return new DelegatingHttpsUrlConnection(url, (HttpsURLConnection) URLStreamHandlerHelper.openConnection(httpsDelegate, url), niddler);
						}

						@Override
						protected URLConnection openConnection(final URL url, final Proxy proxy) throws IOException {
							return new DelegatingHttpsUrlConnection(url, (HttpsURLConnection) URLStreamHandlerHelper.openConnection(httpsDelegate, url, proxy), niddler);
						}
					};
				}
				return null;
			}
		});
	}

	private static boolean installUsingFactoryHacking(@NonNull final Niddler niddler) {
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
										return new DelegatingHttpUrlConnection(url, niddler);
									} else if ("https".equals(protocol)) {
										return new DelegatingHttpsUrlConnection(url, niddler);
									}
									throw new IllegalStateException("Failed miserably!");
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
										return new DelegatingHttpUrlConnection(url, proxy, niddler);
									} else if ("https".equals(protocol)) {
										return new DelegatingHttpsUrlConnection(url, proxy, niddler);
									}
									throw new IllegalStateException("Failed miserably!");
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
		return true;
	}
}
