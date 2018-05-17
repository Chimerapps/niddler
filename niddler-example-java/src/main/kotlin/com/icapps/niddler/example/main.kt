package com.icapps.niddler.example

import com.icapps.niddler.core.JavaNiddler
import com.icapps.niddler.core.Niddler
import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.logging.LogManager


/**
 * @author Nicola Verbeeck
 * @version 1
 */
fun main(args: Array<String>) {

    val logManager = LogManager.getLogManager()
    JavaNiddler::class.java.getResourceAsStream("/logging.properties")
        .use({ `is` -> logManager.readConfiguration(`is`) })

    val niddler = JavaNiddler.Builder()
        .setPort(10299)
        .setCacheSize(1024L * 1024L)
        .setNiddlerInformation(Niddler.NiddlerServerInfo("Niddler-Example",
                                                         "Example java niddler application"))
        .build()

    niddler.start()

    val okHttp =OkHttpClient.Builder()
        .addInterceptor(NiddlerOkHttpInterceptor(niddler))
        .build()

    val request = Request.Builder()
        .get()
        .url("https://jsonplaceholder.typicode.com/posts")
        .build()

    okHttp.newCall(request).execute()

    println("Press return to stop")
    System.`in`.read()

    println("Stopping niddler")
    niddler.close()
}