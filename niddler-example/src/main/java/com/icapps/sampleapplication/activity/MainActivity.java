package com.icapps.sampleapplication.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.icapps.sampleapplication.NiddlerSampleApplication;
import com.icapps.sampleapplication.R;
import com.icapps.sampleapplication.api.ExampleJsonApi;
import com.icapps.sampleapplication.api.ExampleXMLApi;
import com.icapps.sampleapplication.api.Post;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

	private ExampleJsonApi mJsonApi;
	private ExampleXMLApi mXMLApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mJsonApi = ((NiddlerSampleApplication) getApplication()).getJsonPlaceholderApi();
		mXMLApi = ((NiddlerSampleApplication) getApplication()).getXmlPlaceholderApi();

		findViewById(R.id.buttonJson).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mJsonApi.getPosts().enqueue(new Callback<List<Post>>() {
					@Override
					public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
						System.out.println(response.body().size());
					}

					@Override
					public void onFailure(Call<List<Post>> call, Throwable t) {
						System.out.println(t);
					}
				});
			}
		});

		findViewById(R.id.buttonXML).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mXMLApi.getMenu().enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
						Log.w("Response","Got xml response");
					}

					@Override
					public void onFailure(Call<ResponseBody> call, Throwable t) {
						Log.e("Response","Got xml response failure!",t);
					}
				});
			}
		});
	}
}
