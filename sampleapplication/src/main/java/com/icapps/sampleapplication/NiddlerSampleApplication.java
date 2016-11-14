package com.icapps.sampleapplication;

import android.app.Application;

import com.icapps.niddler.Niddler;

/**
 * Created by maartenvangiel on 10/11/2016.
 */
public class NiddlerSampleApplication extends Application {

    private Niddler mNiddler;

    @Override
    public void onCreate() {
        super.onCreate();

        mNiddler = new Niddler.Builder()
                .setPort(1234)
                .build();
    }
}
