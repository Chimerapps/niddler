package com.icapps.niddler.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerMessageBase {

	/**
	 * @return The id of the message. MUST BE unique for every message
	 */
	@NonNull
	String getMessageId();

	/**
	 * @return The request id of the message. Should be the same for all (logically) linked messages. Eg: requests and responses must share the same request tid
	 */
	@NonNull
	String getRequestId();

	/**
	 * @return The system timestamp in milliseconds since epoch when the message was sent/created
	 */
	long getTimestamp();

	/**
	 * @return The headers involved in the message
	 */
	@NonNull
	Map<String, List<String>> getHeaders();

	/**
	 * @return Key-value meta-data to include in the message. Protocol specific meta-data keys start with 'X-Niddler-'. This prefix MUST NOT be used in client code as it will
	 * be stripped before showing in the UI. Null keys or values are NOT supported
	 */
	@NonNull
	Map<String, String> getMetadata();

	/**
	 * Write the message body to the given stream
	 *
	 * @param stream The stream to write to
	 * @throws IOException Can be thrown if writing fails
	 */
	void writeBody(@NonNull final OutputStream stream) throws IOException;

}