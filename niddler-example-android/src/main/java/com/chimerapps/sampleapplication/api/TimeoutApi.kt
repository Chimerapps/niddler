package com.chimerapps.sampleapplication.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

/**
 * @author Nicola Verbeeck
 */
interface TimeoutApi {
    @GET("/200")
    fun getOk(): Call<ResponseBody>

    @GET("/404")
    fun getNotFound(): Call<ResponseBody>

    @GET("/200?sleep=6000")
    fun getOkTimeout(): Call<ResponseBody>
}