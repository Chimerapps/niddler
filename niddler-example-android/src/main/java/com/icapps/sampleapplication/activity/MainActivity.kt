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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mJsonApi: ExampleJsonApi
    private lateinit var mXMLApi: ExampleXMLApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mJsonApi = (application as NiddlerSampleApplication).jsonPlaceholderApi
        mXMLApi = (application as NiddlerSampleApplication).xmlPlaceholderApi

        findViewById<View>(R.id.newActivity).setOnClickListener { startActivity(Intent(this@MainActivity, MainActivity::class.java)) }

        findViewById<View>(R.id.buttonJson).setOnClickListener {
            mJsonApi.posts.enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    Log.w("Response", "Got JSON response")
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    Log.e("Response", "Got JSON response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonXML).setOnClickListener {
            mXMLApi.menu.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got xml response")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got xml response failure!", t)
                }
            })
        }

        findViewById<View>(R.id.buttonPost).setOnClickListener {
            mJsonApi.createPost(makeMessage(), makeAttachment()).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Log.w("Response", "Got xml response")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Response", "Got xml response failure!", t)
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
