package com.icapps.niddler.core;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author Nicola Verbeeck
 * Date 10/11/16.
 */
public interface NiddlerRequest extends NiddlerMessageBase {

    String getUrl();

    String getMethod();

    @Nullable
    StackTraceElement[] getRequestStackTrace();

    @Nullable
    List<String> getRequestContext();

}
