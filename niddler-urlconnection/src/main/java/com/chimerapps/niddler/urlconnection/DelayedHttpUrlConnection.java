package com.chimerapps.niddler.urlconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 */
class DelayedHttpUrlConnection extends HttpURLConnection {

	protected DelayedHttpUrlConnection(final URL url) {
		super(url);
	}

	public InputStream getInputStream() throws IOException {
		throw new UnknownServiceException("protocol doesn't support input");
	}

	public OutputStream getOutputStream() throws IOException {
		throw new UnknownServiceException("protocol doesn't support output");
	}

	@Override
	public void disconnect() {

	}

	@Override
	public boolean usingProxy() {
		return false;
	}

	@Override
	public void connect() throws IOException {
		connected = true;
	}
}
