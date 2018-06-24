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

    if (args.isNotEmpty()) {
        println("Waiting for debugger")
        niddler.debugger().waitForConnection {
            println("Debugger connected")
        }
    }

    val okHttp = OkHttpClient.Builder()
        .addInterceptor(NiddlerOkHttpInterceptor(niddler))
        .build()

    val request = Request.Builder()
        .get()
        .url("https://jsonplaceholder.typicode.com/posts")
        .build()
    val request2 = request.newBuilder().build()

    val response1 = okHttp.newCall(request).execute()
    println("Request 1 executed (" + response1.code() + ")")
    val response2 = okHttp.newCall(request2).execute()
    println("Request 2 executed (" + response2.code() + ")")

    println("Press return to stop")
    System.`in`.read()

    println("Stopping niddler")
    niddler.close()
}