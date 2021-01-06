package com.icapps.niddler.core;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerRequest extends NiddlerMessageBase {

	/**
	 * @return The logical URL of this request
	 */
	@NonNull
	String getUrl();

	/**
	 * @return The logical method of this request.
	 */
	@NonNull
	String getMethod();

	/**
	 * @return Associated request-site stack trace if available/configured
	 */
	@Nullable
	StackTraceElement[] getRequestStackTrace();

	/**
	 * @return Optional request context to display in the UI
	 */
	@Nullable
	List<String> getRequestContext();

}
