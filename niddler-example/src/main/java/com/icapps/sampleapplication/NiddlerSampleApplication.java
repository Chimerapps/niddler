package com.icapps.sampleapplication;

import android.app.Application;
import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.interceptor.NiddlerOkHttpInterceptor;
import com.icapps.sampleapplication.api.ExampleJsonApi;
import com.icapps.sampleapplication.api.ExampleXMLApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by maartenvangiel on 10/11/2016.
 */
public class NiddlerSampleApplication extends Application {

	private Niddler mNiddler;
	private ExampleJsonApi mJsonApi;
	private ExampleXMLApi mXMLApi;

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

			Retrofit jsonRetrofit = new Retrofit.Builder()
					.baseUrl("https://jsonplaceholder.typicode.com")
					.client(okHttpClient)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
			mJsonApi = jsonRetrofit.create(ExampleJsonApi.class);

			Retrofit xmlRetrofit = new Retrofit.Builder()
					.baseUrl("https://raw.githubusercontent.com/")
					.client(okHttpClient)
					.build();
			mXMLApi = xmlRetrofit.create(ExampleXMLApi.class);
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

	public ExampleJsonApi getJsonPlaceholderApi() {
		return mJsonApi;
	}

	public ExampleXMLApi getXmlPlaceholderApi() {
		return mXMLApi;
	}
}
