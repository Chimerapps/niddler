package com.icapps.niddler.core;

import com.icapps.niddler.util.LogUtil;
import com.icapps.niddler.utils.JavaLogUtil;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public class JavaNiddler extends Niddler implements Niddler.PlatformNiddler {

	JavaNiddler(final String password, final int port, final long cacheSize, final NiddlerServerInfo niddlerServerInfo) {
		super(password, port, cacheSize, niddlerServerInfo);
		mNiddlerImpl.setPlatform(this);
	}

	@Override
	public void closePlatform() {
	}

	public static class Builder extends Niddler.Builder<JavaNiddler> {

		public Builder() {
			LogUtil.instance = new JavaLogUtil();
		}

		@Override
		public JavaNiddler build() {
			return new JavaNiddler(mPassword, mPort, mCacheSize, mNiddlerServerInfo);
		}

	}
}
