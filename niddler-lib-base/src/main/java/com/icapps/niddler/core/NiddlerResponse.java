package com.icapps.niddler.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerResponse extends NiddlerMessageBase {

	Integer getStatusCode();

	@NonNull
	String getStatusLine();

	@NonNull
	String getHttpVersion();

	@Nullable
	NiddlerRequest actualNetworkRequest();

	@Nullable
	NiddlerResponse actualNetworkReply();

	int getWriteTime();

	int getReadTime();

	int getWaitTime();
}
