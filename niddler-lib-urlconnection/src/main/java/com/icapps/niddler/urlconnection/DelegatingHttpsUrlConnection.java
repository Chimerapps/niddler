package com.icapps.niddler.urlconnection;

import com.icapps.niddler.core.Niddler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * @author Nicola Verbeeck
 */
class DelegatingHttpsUrlConnection extends HttpsURLConnection {

	private final HttpsURLConnection delegate;
	private final DelegatingHttpUrlConnection httpDelegate;

	public DelegatingHttpsUrlConnection(@NonNull final URL url, @NonNull final HttpsURLConnection delegate, @NonNull final Niddler niddler) {
		super(url);
		this.delegate = delegate;
		this.httpDelegate = new DelegatingHttpUrlConnection(url, delegate, niddler);
	}

	public DelegatingHttpsUrlConnection(@NonNull final URL url, @NonNull final Niddler niddler) throws IOException {
		this(url, (HttpsURLConnection) url.openConnection(), niddler);
	}

	public DelegatingHttpsUrlConnection(@NonNull final URL url, Proxy proxy, @NonNull final Niddler niddler) throws IOException {
		this(url, (HttpsURLConnection) url.openConnection(proxy), niddler);
	}

	@Override
	public String getCipherSuite() {
		return delegate.getCipherSuite();
	}

	@Override
	public Certificate[] getLocalCertificates() {
		return delegate.getLocalCertificates();
	}

	@Override
	public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
		return delegate.getServerCertificates();
	}

	@Override
	public void disconnect() {
		httpDelegate.disconnect();
	}

	@Override
	public boolean usingProxy() {
		return httpDelegate.usingProxy();
	}

	@Override
	public void connect() throws IOException {
		httpDelegate.connect();
	}

	@Override
	public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
		return delegate.getPeerPrincipal();
	}

	@Override
	public Principal getLocalPrincipal() {
		return delegate.getLocalPrincipal();
	}

	@Override
	public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
		delegate.setHostnameVerifier(hostnameVerifier);
	}

	@Override
	public HostnameVerifier getHostnameVerifier() {
		return delegate.getHostnameVerifier();
	}

	@Override
	public void setSSLSocketFactory(final SSLSocketFactory sslSocketFactory) {
		delegate.setSSLSocketFactory(sslSocketFactory);
	}

	@Override
	public SSLSocketFactory getSSLSocketFactory() {
		return delegate.getSSLSocketFactory();
	}

	@Override
	public String getHeaderFieldKey(final int i) {
		return httpDelegate.getHeaderFieldKey(i);
	}

	@Override
	public void setFixedLengthStreamingMode(final int i) {
		httpDelegate.setFixedLengthStreamingMode(i);
	}

	@Override
	public void setFixedLengthStreamingMode(final long l) {
		httpDelegate.setFixedLengthStreamingMode(l);
	}

	@Override
	public void setChunkedStreamingMode(final int i) {
		httpDelegate.setChunkedStreamingMode(i);
	}

	@Override
	public String getHeaderField(final int i) {
		return httpDelegate.getHeaderField(i);
	}

	@Override
	public void setInstanceFollowRedirects(final boolean b) {
		httpDelegate.setInstanceFollowRedirects(b);
	}

	@Override
	public boolean getInstanceFollowRedirects() {
		return httpDelegate.getInstanceFollowRedirects();
	}

	@Override
	public void setRequestMethod(final String s) throws ProtocolException {
		httpDelegate.setRequestMethod(s);
	}

	@Override
	public String getRequestMethod() {
		return httpDelegate.getRequestMethod();
	}

	@Override
	public int getResponseCode() throws IOException {
		return httpDelegate.getResponseCode();
	}

	@Override
	public String getResponseMessage() throws IOException {
		return httpDelegate.getResponseMessage();
	}

	@Override
	public long getHeaderFieldDate(final String s, final long l) {
		return httpDelegate.getHeaderFieldDate(s, l);
	}

	@Override
	public Permission getPermission() throws IOException {
		return httpDelegate.getPermission();
	}

	@Override
	public InputStream getErrorStream() {
		return httpDelegate.getErrorStream();
	}

	@Override
	public void setConnectTimeout(final int i) {
		httpDelegate.setConnectTimeout(i);
	}

	@Override
	public int getConnectTimeout() {
		return httpDelegate.getConnectTimeout();
	}

	@Override
	public void setReadTimeout(final int i) {
		httpDelegate.setReadTimeout(i);
	}

	@Override
	public int getReadTimeout() {
		return httpDelegate.getReadTimeout();
	}

	@Override
	public URL getURL() {
		return httpDelegate.getURL();
	}

	@Override
	public int getContentLength() {
		return httpDelegate.getContentLength();
	}

	@RequiresApi(api = 24)
	@Override
	public long getContentLengthLong() {
		return httpDelegate.getContentLengthLong();
	}

	@Override
	public String getContentType() {
		return httpDelegate.getContentType();
	}

	@Override
	public String getContentEncoding() {
		return httpDelegate.getContentEncoding();
	}

	@Override
	public long getExpiration() {
		return httpDelegate.getExpiration();
	}

	@Override
	public long getDate() {
		return httpDelegate.getDate();
	}

	@Override
	public long getLastModified() {
		return httpDelegate.getLastModified();
	}

	@Override
	public String getHeaderField(final String s) {
		return httpDelegate.getHeaderField(s);
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		return httpDelegate.getHeaderFields();
	}

	@Override
	public int getHeaderFieldInt(final String s, final int i) {
		return httpDelegate.getHeaderFieldInt(s, i);
	}

	@RequiresApi(api = 24)
	@Override
	public long getHeaderFieldLong(final String s, final long l) {
		return httpDelegate.getHeaderFieldLong(s, l);
	}

	@Override
	public Object getContent() throws IOException {
		return httpDelegate.getContent();
	}

	@Override
	public Object getContent(final Class[] classes) throws IOException {
		return httpDelegate.getContent(classes);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return httpDelegate.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return httpDelegate.getOutputStream();
	}

	@Override
	public String toString() {
		return httpDelegate.toString();
	}

	@Override
	public void setDoInput(final boolean b) {
		httpDelegate.setDoInput(b);
	}

	@Override
	public boolean getDoInput() {
		return httpDelegate.getDoInput();
	}

	@Override
	public void setDoOutput(final boolean b) {
		httpDelegate.setDoOutput(b);
	}

	@Override
	public boolean getDoOutput() {
		return httpDelegate.getDoOutput();
	}

	@Override
	public void setAllowUserInteraction(final boolean b) {
		httpDelegate.setAllowUserInteraction(b);
	}

	@Override
	public boolean getAllowUserInteraction() {
		return httpDelegate.getAllowUserInteraction();
	}

	@Override
	public void setUseCaches(final boolean b) {
		httpDelegate.setUseCaches(b);
	}

	@Override
	public boolean getUseCaches() {
		return httpDelegate.getUseCaches();
	}

	@Override
	public void setIfModifiedSince(final long l) {
		httpDelegate.setIfModifiedSince(l);
	}

	@Override
	public long getIfModifiedSince() {
		return httpDelegate.getIfModifiedSince();
	}

	@Override
	public boolean getDefaultUseCaches() {
		return httpDelegate.getDefaultUseCaches();
	}

	@Override
	public void setDefaultUseCaches(final boolean b) {
		httpDelegate.setDefaultUseCaches(b);
	}

	@Override
	public void setRequestProperty(final String s, final String s1) {
		httpDelegate.setRequestProperty(s, s1);
	}

	@Override
	public void addRequestProperty(final String s, final String s1) {
		httpDelegate.addRequestProperty(s, s1);
	}

	@Override
	public String getRequestProperty(final String s) {
		return httpDelegate.getRequestProperty(s);
	}

	@Override
	public Map<String, List<String>> getRequestProperties() {
		return httpDelegate.getRequestProperties();
	}
}
