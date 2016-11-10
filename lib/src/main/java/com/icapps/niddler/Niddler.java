package com.icapps.niddler;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public interface Niddler {

	void addRequest(final NiddlerRequest request);

	void addResponse(final NiddlerResponse response);

}
