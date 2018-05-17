package com.icapps.sampleapplication.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by maartenvangiel on 14/11/2016.
 */
public interface ExampleJsonApi {

    @GET("/posts")
    Call<List<Post>> getPosts();

    @GET("/posts/{id}")
    Call<Post> getPost(@Path("id") final int id);

}
