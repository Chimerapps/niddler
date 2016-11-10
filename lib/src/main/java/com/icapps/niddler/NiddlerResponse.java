package com.icapps.niddler;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public interface NiddlerResponse {

	String getRequestId();

	Map<String, List<String>> getHeaders();

	Integer getStatusCode();

	void writeBody(final OutputStream stream);

}
