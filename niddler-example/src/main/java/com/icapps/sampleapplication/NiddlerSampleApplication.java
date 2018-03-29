package com.icapps.sampleapplication;

import android.app.Application;

import com.icapps.niddler.core.Niddler;
import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor;
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

		final Niddler niddler = new Niddler.Builder()
				.setPort(0)
				.setNiddlerInformation(Niddler.NiddlerServerInfo.fromApplication(this))
				.build();

		niddler.attachToApplication(this);

		final OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new NiddlerOkHttpInterceptor(niddler))
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
	}

	public ExampleJsonApi getJsonPlaceholderApi() {
		return mJsonApi;
	}

	public ExampleXMLApi getXmlPlaceholderApi() {
		return mXMLApi;
	}

}
