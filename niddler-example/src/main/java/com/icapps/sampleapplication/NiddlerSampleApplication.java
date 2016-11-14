package com.icapps.sampleapplication;

import android.app.Application;

import com.icapps.niddler.Niddler;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by maartenvangiel on 10/11/2016.
 */
public class NiddlerSampleApplication extends Application {

    private Niddler mNiddler;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mNiddler = new Niddler.Builder()
                    .setPort(1234)
                    .build();

            mNiddler.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            mNiddler.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Niddler getNiddler() {
        return mNiddler;
    }
}
