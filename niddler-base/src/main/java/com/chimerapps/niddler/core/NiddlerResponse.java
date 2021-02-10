package com.chimerapps.niddler.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerResponse extends NiddlerMessageBase {

	/**
	 * @return The status code of the response. Eg: For HTTP this is the HTTP response code
	 */
	@NonNull
	Integer getStatusCode();

	/**
	 * @return The status line (message) of the response. Eg: For HTTP this is the HTTP response message
	 */
	@NonNull
	String getStatusLine();

	/**
	 * @return The http version used to handle the request. Can be empty if the protocol is not using http
	 */
	@NonNull
	String getHttpVersion();

	/**
	 * @return If set, the actual request that was sent over the network
	 */
	@Nullable
	NiddlerRequest actualNetworkRequest();

	/**
	 * @return If set, the actual response that was sent over the network
	 */
	@Nullable
	NiddlerResponse actualNetworkReply();

	/**
	 * @return If set, the stacktrace of an exception that was thrown during the execution of the request
	 */
	@Nullable
	StackTraceElement[] getErrorStackTrace();

	/**
	 * @return The time, in milliseconds, it took for the request to write to the network. Use -1 if unknown
	 */
	int getWriteTime();

	/**
	 * @return The time, in milliseconds, it took for the response to read from the network. Use -1 if unknown
	 */
	int getReadTime();

	/**
	 * @return The time, in milliseconds, it took for the response to arrive after it was written to the network. Use -1 if unknown
	 */
	int getWaitTime();
}
