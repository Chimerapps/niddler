package com.icapps.sampleapplication;

import android.app.Application;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.interceptor.NiddlerOkHttpInterceptor;
import com.icapps.sampleapplication.api.ExampleApi;

import java.io.IOException;
import java.net.UnknownHostException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by maartenvangiel on 10/11/2016.
 */
public class NiddlerSampleApplication extends Application {

    private Niddler mNiddler;
    private ExampleApi mApi;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mNiddler = new Niddler.Builder()
                    .setPort(6555)
                    .build();

            mNiddler.start();

            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new NiddlerOkHttpInterceptor(mNiddler))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://jsonplaceholder.typicode.com")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mApi = retrofit.create(ExampleApi.class);
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

    public ExampleApi getJsonPlaceholderApi() {
        return mApi;
    }
}
