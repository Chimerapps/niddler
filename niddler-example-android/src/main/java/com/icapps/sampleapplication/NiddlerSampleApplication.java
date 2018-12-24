package com.icapps.sampleapplication;

import android.app.Application;

import com.icapps.niddler.core.AndroidNiddler;
import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor;
import com.icapps.niddler.retrofit.NiddlerRetrofitCallInjector;
import com.icapps.sampleapplication.api.ExampleJsonApi;
import com.icapps.sampleapplication.api.ExampleXMLApi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by maartenvangiel on 10/11/2016.
 */
public class NiddlerSampleApplication extends Application {

    private ExampleJsonApi mJsonApi;
    private ExampleXMLApi mXMLApi;

    @Override
    public void onCreate() {
        super.onCreate();

        final AndroidNiddler niddler = new AndroidNiddler.Builder()
                .setPort(0)
                .setNiddlerInformation(AndroidNiddler.fromApplication(this))
                .setMaxStackTraceSize(10)
                .build();

        niddler.attachToApplication(this);

        final NiddlerOkHttpInterceptor okHttpInterceptor = new NiddlerOkHttpInterceptor(niddler);
        okHttpInterceptor.blacklist(".*raw\\.githubusercontent\\.com.*");

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(okHttpInterceptor)
                .build();

        Retrofit.Builder jsonRetrofitBuilder = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create());
        NiddlerRetrofitCallInjector.inject(jsonRetrofitBuilder, niddler, okHttpClient);
        final Retrofit jsonRetrofit = jsonRetrofitBuilder.build();
        mJsonApi = jsonRetrofit.create(ExampleJsonApi.class);

        Retrofit xmlRetrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client(okHttpClient)
                .build();
        mXMLApi = xmlRetrofit.create(ExampleXMLApi.class);
    }

    public ExampleJsonApi getJsonPlaceholderApi() {
        return mJsonApi;
    }

    public ExampleXMLApi getXmlPlaceholderApi() {
        return mXMLApi;
    }

}
