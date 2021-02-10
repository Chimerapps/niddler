package com.chimerapps.niddler.core;

import com.chimerapps.niddler.util.LogUtil;
import com.chimerapps.niddler.utils.JavaLogUtil;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public class JavaNiddler extends Niddler implements Niddler.PlatformNiddler {

	JavaNiddler(final String password, final int port, final long cacheSize,
			final NiddlerServerInfo niddlerServerInfo, final int maxStackTraceSize) {
		super(password, port, cacheSize, niddlerServerInfo, maxStackTraceSize);
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
			return new JavaNiddler(mPassword, mPort, mCacheSize, mNiddlerServerInfo, mMaxStackTraceSize);
		}

	}
}
