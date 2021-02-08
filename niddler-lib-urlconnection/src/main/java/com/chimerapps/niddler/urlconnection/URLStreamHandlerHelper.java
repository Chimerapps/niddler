package com.chimerapps.niddler.urlconnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

import androidx.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 */
final class URLStreamHandlerHelper {

	public static HttpURLConnection openConnection(@NonNull final URLStreamHandler handler, @NonNull final URL url) throws IOException {
		return (HttpURLConnection) new URL(null, url.toString(), handler).openConnection();
	}

	public static HttpURLConnection openConnection(@NonNull final URLStreamHandler handler, @NonNull final URL url, @NonNull final Proxy proxy) throws IOException {
		return (HttpURLConnection) new URL(null, url.toString(), handler).openConnection(proxy);
	}

}
