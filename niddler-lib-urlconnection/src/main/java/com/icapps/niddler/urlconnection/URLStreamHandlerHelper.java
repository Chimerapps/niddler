package com.icapps.niddler.urlconnection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

/**
 * @author Nicola Verbeeck
 */
final class URLStreamHandlerHelper {

	public static HttpURLConnection openConnection(URLStreamHandler handler, final URL url) throws IOException {
		try {
			final Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
			method.setAccessible(true);
			return (HttpURLConnection) method.invoke(handler, url);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpURLConnection openConnection(URLStreamHandler handler, final URL url, final Proxy proxy) throws IOException {
		try {
			final Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
			method.setAccessible(true);
			return (HttpURLConnection) method.invoke(handler, url, proxy);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
