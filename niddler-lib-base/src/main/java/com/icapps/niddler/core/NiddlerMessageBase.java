package com.icapps.niddler.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerMessageBase {

	String getMessageId();

	String getRequestId();

	long getTimestamp();

	Map<String, List<String>> getHeaders();

	void writeBody(final OutputStream stream) throws IOException;

}
