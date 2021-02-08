package com.chimerapps.sampleapplication.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by maartenvangiel on 14/11/2016.
 */
interface ExampleXMLApi {

    @get:GET("/bcantoni/yql/master/lorem.ipsum.xml")
    val menu: Call<ResponseBody>

}
