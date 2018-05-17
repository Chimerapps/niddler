package com.icapps.niddler.core;

import com.icapps.niddler.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
final class MessagesCache {

	private final List<String> mMessages;
	private final long mMaxCacheSize;
	private long mCacheSize;

	MessagesCache(final long maxCacheSize) {
		mMessages = new LinkedList<>();
		mMaxCacheSize = maxCacheSize;
	}

	void clear() {
		synchronized (mMessages) {
			mMessages.clear();
			mCacheSize = 0;
		}
	}

	void put(final String message) {
		if (mMaxCacheSize <= 0) {
			return;
		}

		final long size = StringUtil.calculateMemoryUsage(message);

		synchronized (mMessages) {
			while ((size + mCacheSize) > mMaxCacheSize) {
				if (!evictOld()) { //No more messages to remove, the message is too large for the cache -> do not add
					return;
				}
			}
			mCacheSize += size;
			mMessages.add(message);
		}
	}

	Collection<String> get() {
		synchronized (mMessages) {
			return new ArrayList<>(mMessages);
		}
	}

	private boolean evictOld() {
		if (mMessages.isEmpty()) {
			return false;
		}
		final String message = mMessages.remove(0);
		mCacheSize -= StringUtil.calculateMemoryUsage(message);
		return true;
	}


}
