package com.icapps.niddler.core;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerRequest extends NiddlerMessageBase {

	String getUrl();

	String getMethod();

}
