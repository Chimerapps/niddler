package com.icapps.sampleapplication

import android.app.Application

import com.chimerapps.niddler.core.AndroidNiddler
import com.chimerapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import com.chimerapps.niddler.retrofit.NiddlerRetrofitCallInjector
import com.chimerapps.niddler.urlconnection.NiddlerUrlConnectionHandler
import com.icapps.sampleapplication.api.ExampleJsonApi
import com.icapps.sampleapplication.api.ExampleXMLApi
import com.icapps.sampleapplication.api.TimeoutApi
import okhttp3.Cache

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by maartenvangiel on 10/11/2016.
 */
class NiddlerSampleApplication : Application() {

    lateinit var jsonPlaceholderApi: ExampleJsonApi
        private set
    lateinit var xmlPlaceholderApi: ExampleXMLApi
        private set
    lateinit var timeoutApi: TimeoutApi

    override fun onCreate() {
        super.onCreate()

        val niddler = AndroidNiddler.Builder()
                .setPort(0)
                .setNiddlerInformation(AndroidNiddler.fromApplication(this))
                .setMaxStackTraceSize(10)
                .build()

        niddler.attachToApplication(this)

        NiddlerUrlConnectionHandler.install(niddler);

        val okHttpInterceptor = NiddlerOkHttpInterceptor(niddler, "Default", true)
        okHttpInterceptor.blacklist(".*raw\\.githubusercontent\\.com.*")

        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .cache(Cache(cacheDir,10485760L))
                .addInterceptor(okHttpInterceptor)
                .build()

        val jsonRetrofitBuilder = Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
        NiddlerRetrofitCallInjector.inject(jsonRetrofitBuilder, niddler, okHttpClient)
        val jsonRetrofit = jsonRetrofitBuilder.build()
        jsonPlaceholderApi = jsonRetrofit.create(ExampleJsonApi::class.java)

        val xmlRetrofit = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client(okHttpClient)
                .build()
        xmlPlaceholderApi = xmlRetrofit.create(ExampleXMLApi::class.java)

        val timeoutRetrofit = Retrofit.Builder()
                .baseUrl("https://httpstat.us/")
                .client(okHttpClient)
                .build()
        timeoutApi = timeoutRetrofit.create(TimeoutApi::class.java)
    }

}
