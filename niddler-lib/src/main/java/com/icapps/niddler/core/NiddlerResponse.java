package com.icapps.niddler.core;

import android.support.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerResponse extends NiddlerMessageBase {

	Integer getStatusCode();

	@Nullable
	NiddlerRequest actualNetworkRequest();

	@Nullable
	NiddlerResponse actualNetworkReply();
}
