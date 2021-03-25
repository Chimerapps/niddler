package com.chimerapps.niddler.utils;

import com.chimerapps.niddler.util.LogUtil;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public class JavaLogUtil extends LogUtil {

	@Override
	protected void doLog(final int level, final String tag, final String message, final Throwable error) {
		final LogRecord record = new LogRecord(mapLevel(level), message);
		record.setThrown(error);
		Logger.getLogger(tag).log(record);
	}

	@Override
	protected void doLogStartup(String message) {
		System.out.println(message);
	}

	@Override
	protected boolean doIsLoggable(final String tag, final int level) {
		return Logger.getLogger(tag).isLoggable(mapLevel(level));
	}

	private static Level mapLevel(final int level) {
		switch (level) {
			case VERBOSE:
				return Level.FINEST;
			case DEBUG:
				return Level.FINE;
			case INFO:
				return Level.INFO;
			case WARN:
				return Level.WARNING;
			case ERROR:
			default:
				return Level.SEVERE;
		}
	}

}
