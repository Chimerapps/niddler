package com.icapps.sampleapplication.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Created by maartenvangiel on 14/11/2016.
 */
interface ExampleJsonApi {

    @get:GET("/posts")
    val posts: Call<List<Post>>

    @GET("/posts/{id}")
    fun getPost(@Path("id") id: Int): Call<Post>

    @Multipart
    @POST("/posts")
    fun createPost(@Part message: MultipartBody.Part, @Part attachment: MultipartBody.Part): Call<ResponseBody>

}
