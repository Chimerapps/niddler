package com.icapps.sampleapplication.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.icapps.sampleapplication.NiddlerSampleApplication
import com.icapps.sampleapplication.R
import com.icapps.sampleapplication.api.ExampleJsonApi
import com.icapps.sampleapplication.api.ExampleXMLApi
import com.icapps.sampleapplication.api.Post
import com.icapps.sampleapplication.api.TimeoutApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var jsonApi: ExampleJsonApi
    private lateinit var theXMLApi: ExampleXMLApi
    private lateinit var timeoutApi: TimeoutApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jsonApi = (application as NiddlerSampleApplication).jsonPlaceholderApi
        theXMLApi = (application as NiddlerSampleApplication).xmlPlaceholderApi
        timeoutApi = (application as NiddlerSampleApplication).timeoutApi

        findViewById<View>(R.id.newActivity).setOnClickListener { startActivity(Intent(this@MainActivity, MainActivity::class.java)) }

        findViewById<View>(R.id.buttonJson).setOnClickListener {
            jsonApi.posts.enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    Log.w("Response", "Got JSON response")
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    Log.e("Response", "Got JSON response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonXML).setOnClickListener {
            theXMLApi.menu.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got xml response")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got xml response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonPost).setOnClickListener {
            jsonApi.createPost(makeMessage(), makeAttachment()).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got xml response")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got xml response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonTimeoutOk).setOnClickListener {
            timeoutApi.getOk().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got timeout response: ${response.code()}")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got timeout response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonTimeoutNotFound).setOnClickListener {
            timeoutApi.getNotFound().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got timeout response ${response.code()}")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got timeout response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonTimeoutTimeout).setOnClickListener {
            timeoutApi.getOkTimeout().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got timeout response: ${response.code()}")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got timeout response failure!", t)
                }
            })
        }
    }

    private fun makeMessage(): MultipartBody.Part {
        return MultipartBody.Part.create("{\"body\":\"This is the json part of a multipart upload example\"}".toRequestBody("application/json".toMediaType()))
    }

    private fun makeAttachment(): MultipartBody.Part {
        val imageBytes = assets.open("image.jpeg").use { it.readBytes() }

        return MultipartBody.Part.create(imageBytes.toRequestBody("image/jpeg".toMediaType(), 0, imageBytes.size))
    }
}
