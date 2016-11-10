package com.icapps.niddler;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public interface NiddlerRequest {

	String getRequestId();

	String getUrl();

	Map<String, List<String>> getHeaders();

	String getMethod();

	void writeBody(final OutputStream stream);

}
