package com.icapps.niddler.urlconnection;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.core.NiddlerRequest;
import com.icapps.niddler.core.NiddlerResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author Nicola Verbeeck
 */
class DelegatingHttpUrlConnection extends HttpURLConnection {

	@NonNull
	private final HttpURLConnection delegate;
	@NonNull
	private final Map<String, List<String>> requestHeaders = new LinkedHashMap<>();
	@NonNull
	private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	@Nullable
	private byte[] inputBuffer;
	private boolean bufferIsError;
	private boolean outputFinished = false;
	private boolean sentRequest = false;
	private boolean sendResponse = false;
	private String requestId;
	@NonNull
	private final Niddler niddler;

	public DelegatingHttpUrlConnection(@NonNull final URL url, @NonNull final HttpURLConnection delegate, @NonNull final Niddler niddler) {
		super(url);
		this.delegate = delegate;
		this.niddler = niddler;
	}

	public DelegatingHttpUrlConnection(@NonNull final URL url, @NonNull final Niddler niddler) throws IOException {
		this(url, (HttpURLConnection) url.openConnection(), niddler);
	}

	public DelegatingHttpUrlConnection(@NonNull final URL url, Proxy proxy, @NonNull final Niddler niddler) throws IOException {
		this(url, (HttpURLConnection) url.openConnection(proxy), niddler);
	}

	@Override
	public void setFixedLengthStreamingMode(final int i) {
		delegate.setFixedLengthStreamingMode(i);
	}

	@Override
	public void setFixedLengthStreamingMode(final long l) {
		delegate.setFixedLengthStreamingMode(l);
	}

	@Override
	public void setChunkedStreamingMode(final int i) {
		delegate.setChunkedStreamingMode(i);
	}

	@Override
	public void setInstanceFollowRedirects(final boolean b) {
		delegate.setInstanceFollowRedirects(b);
	}

	@Override
	public boolean getInstanceFollowRedirects() {
		return delegate.getInstanceFollowRedirects();
	}

	@Override
	public void setRequestMethod(final String s) throws ProtocolException {
		delegate.setRequestMethod(s);
	}

	@Override
	public String getRequestMethod() {
		return delegate.getRequestMethod();
	}

	@Override
	public int getResponseCode() throws IOException {
		maybeSendRequest();
		try {
			return delegate.getResponseCode();
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public String getResponseMessage() throws IOException {
		maybeSendRequest();
		try {
			return delegate.getResponseMessage();
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public InputStream getErrorStream() {
		maybeSendRequest();
		try {
			if (bufferIsError && inputBuffer != null) {
				return new ByteArrayInputStream(inputBuffer);
			}

			final InputStream error = delegate.getErrorStream();
			if (error == null) {
				return null;
			}

			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				copyStream(error, out);
				inputBuffer = out.toByteArray();
				bufferIsError = true;
				error.close();
				return new ByteArrayInputStream(inputBuffer);
			} catch (final Throwable e) {
				return null;
			}
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public void disconnect() {
		delegate.disconnect();
	}

	@Override
	public boolean usingProxy() {
		return delegate.usingProxy();
	}

	@Override
	public void connect() throws IOException {
		maybeSendRequest();
		try {
			delegate.connect();
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public void setConnectTimeout(final int i) {
		delegate.setConnectTimeout(i);
	}

	@Override
	public int getConnectTimeout() {
		return delegate.getConnectTimeout();
	}

	@Override
	public void setReadTimeout(final int i) {
		delegate.setReadTimeout(i);
	}

	@Override
	public int getReadTimeout() {
		return delegate.getReadTimeout();
	}

	@Override
	public URL getURL() {
		return delegate.getURL();
	}

	@Override
	public int getContentLength() {
		return delegate.getContentLength();
	}

	@RequiresApi(api = 24)
	@Override
	public long getContentLengthLong() {
		return delegate.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public String getContentEncoding() {
		return delegate.getContentEncoding();
	}

	@Override
	public long getExpiration() {
		return delegate.getExpiration();
	}

	@Override
	public long getDate() {
		return delegate.getDate();
	}

	@Override
	public long getLastModified() {
		return delegate.getLastModified();
	}

	@Override
	public String getHeaderField(final String s) {
		maybeSendRequest();
		try {
			return delegate.getHeaderField(s);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		maybeSendRequest();
		try {
			return delegate.getHeaderFields();
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public int getHeaderFieldInt(final String s, final int i) {
		maybeSendRequest();
		try {
			return delegate.getHeaderFieldInt(s, i);
		} finally {
			maybeSendResponse();
		}
	}

	@RequiresApi(api = 24)
	@Override
	public long getHeaderFieldLong(final String s, final long l) {
		maybeSendRequest();
		try {
			return delegate.getHeaderFieldLong(s, l);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public long getHeaderFieldDate(final String s, final long l) {
		maybeSendRequest();
		try {
			return delegate.getHeaderFieldDate(s, l);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public String getHeaderFieldKey(final int i) {
		maybeSendRequest();
		try {
			return delegate.getHeaderFieldKey(i);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public String getHeaderField(final int i) {
		maybeSendRequest();
		try {
			return delegate.getHeaderField(i);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public Object getContent() throws IOException {
		maybeSendRequest();
		try {
			return delegate.getContent();
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public Object getContent(final Class[] classes) throws IOException {
		maybeSendRequest();
		try {
			return delegate.getContent(classes);
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public Permission getPermission() throws IOException {
		return delegate.getPermission();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		maybeSendRequest();
		try {
			if (!bufferIsError && inputBuffer != null) {
				return new ByteArrayInputStream(inputBuffer);
			}

			final InputStream input = delegate.getInputStream();
			if (input == null) {
				return null;
			}

			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				copyStream(input, out);
				inputBuffer = out.toByteArray();
				bufferIsError = false;
				input.close();
				return new ByteArrayInputStream(inputBuffer);
			} catch (final Throwable e) {
				return null;
			}
		} finally {
			maybeSendResponse();
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (!doOutput) {
			throw new IOException("Not configured for output");
		}
		return new OutputStream() {

			@Override
			public void write(final int i) {
				outputBuffer.write(i);
			}

			@Override
			public void write(final byte[] bytes, final int i, final int i1) {
				outputBuffer.write(bytes, i, i1);
			}

			@Override
			public void close() throws IOException {
				outputFinished = true;
				maybeSendRequest();
				try {
					final OutputStream out = delegate.getOutputStream();
					out.write(outputBuffer.toByteArray());
					out.close();
				} finally {
					maybeSendResponse();
				}
			}
		};
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void setDoInput(final boolean b) {
		delegate.setDoInput(b);
	}

	@Override
	public boolean getDoInput() {
		return delegate.getDoInput();
	}

	@Override
	public void setDoOutput(final boolean b) {
		delegate.setDoOutput(b);
	}

	@Override
	public boolean getDoOutput() {
		return delegate.getDoOutput();
	}

	@Override
	public void setAllowUserInteraction(final boolean b) {
		delegate.setAllowUserInteraction(b);
	}

	@Override
	public boolean getAllowUserInteraction() {
		return delegate.getAllowUserInteraction();
	}

	@Override
	public void setUseCaches(final boolean b) {
		delegate.setUseCaches(b);
	}

	@Override
	public boolean getUseCaches() {
		return delegate.getUseCaches();
	}

	@Override
	public void setIfModifiedSince(final long l) {
		delegate.setIfModifiedSince(l);
		if (l == 0L) {
			requestHeaders.remove("if-modified-since");
		} else {
			final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			final String formatted = format.format(new Date(l));
			final List<String> list = new ArrayList<>();
			list.add(formatted);
			requestHeaders.put("if-modified-since", list);
		}
	}

	@Override
	public long getIfModifiedSince() {
		return delegate.getIfModifiedSince();
	}

	@Override
	public boolean getDefaultUseCaches() {
		return delegate.getDefaultUseCaches();
	}

	@Override
	public void setDefaultUseCaches(final boolean b) {
		delegate.setDefaultUseCaches(b);
	}

	@Override
	public void setRequestProperty(final String key, final String value) {
		delegate.setRequestProperty(key, value);

		if (key != null && value != null) {
			final List<String> list = new ArrayList<>();
			list.add(value);
			requestHeaders.put(key.toLowerCase(Locale.ENGLISH), list);
		}
	}

	@Override
	public void addRequestProperty(final String key, final String value) {
		delegate.addRequestProperty(key, value);

		if (key != null && value != null) {
			List<String> old = requestHeaders.get(key.toLowerCase(Locale.ENGLISH));
			if (old == null) {
				old = new ArrayList<>();
				requestHeaders.put(key.toLowerCase(Locale.ENGLISH), old);
			}
			old.add(value);
		}
	}

	@Override
	public String getRequestProperty(final String s) {
		return delegate.getRequestProperty(s);
	}

	@Override
	public Map<String, List<String>> getRequestProperties() {
		return delegate.getRequestProperties();
	}

	private void maybeSendRequest() {
		if (sentRequest) {
			return;
		}
		if (doOutput && !outputFinished) {
			return;
		}

		sentRequest = true;

		requestId = UUID.randomUUID().toString();
		final NiddlerRequest request = new NiddlerUrlConnectionRequest(
				requestId,
				url.toString(),
				method,
				requestHeaders,
				Collections.<String, String>emptyMap(),
				doOutput ? outputBuffer.toByteArray() : null,
				niddler.isStackTracingEnabled() ? new IOException().getStackTrace() : null,
				/*requestContext*/null
		);

		niddler.logRequest(request);
	}

	private void maybeSendResponse() {
		if (sendResponse) {
			return;
		}
		sendResponse = true;

		int responseCode = -1;
		try {
			responseCode = delegate.getResponseCode();
		} catch (final IOException ignored) {
		}
		String responseMessage = "";
		try {
			responseMessage = delegate.getResponseMessage();
		} catch (final IOException ignored) {
		}
		final Map<String, List<String>> headers = sanitizeHeaders(delegate.getHeaderFields());
		int waitTime = -1;
		try {
			waitTime = (int) (Long.parseLong(headers.get("x-android-received-millis").get(0)) - Long.parseLong(headers.get("x-android-sent-millis").get(0)));
		} catch (final Throwable ignored) {
		}

		final NiddlerResponse response = new NiddlerUrlConnectionResponse(
				requestId,
				headers,
				Collections.<String, String>emptyMap(),
				inputBuffer,
				responseCode,
				responseMessage,
				headers.containsKey("x-android-selected-protocol") ? headers.get("x-android-selected-protocol").get(0) : "http/1.1",
				waitTime
		);
		niddler.logResponse(response);
	}

	private Map<String, List<String>> sanitizeHeaders(final Map<String, List<String>> headerFields) {
		final Map<String, List<String>> ret = new LinkedHashMap<>();
		for (final Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			if (entry.getKey() != null) {
				ret.put(entry.getKey().toLowerCase(Locale.ENGLISH), entry.getValue());
			}
		}
		return ret;
	}

	private static void copyStream(@NonNull InputStream from, @NonNull OutputStream to) throws IOException {
		byte[] buf = new byte[0x1000]; //4k temp buffer
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
		}
	}

}
