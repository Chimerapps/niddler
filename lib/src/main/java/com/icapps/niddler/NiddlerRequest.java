package com.icapps.niddler;

import java.util.List;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public interface NiddlerRequest {

	String getId();

	String getUrl();

	Map<String, List<String>> getHeaders();

}
