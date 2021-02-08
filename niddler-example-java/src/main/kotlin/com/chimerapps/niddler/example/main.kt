package com.chimerapps.niddler.example

import com.chimerapps.niddler.core.JavaNiddler
import com.chimerapps.niddler.core.Niddler
import com.chimerapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.util.Random
import java.util.logging.LogManager
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * @author Nicola Verbeeck
 * @version 1
 */
fun main(args: Array<String>) {

    val logManager = LogManager.getLogManager()
    JavaNiddler::class.java.getResourceAsStream("/logging.properties")
            .use { `is` -> logManager.readConfiguration(`is`) }

    val niddler = JavaNiddler.Builder()
            .setPort(0)
            .setCacheSize(1024L * 1024L)
            .setNiddlerInformation(Niddler.NiddlerServerInfo("Niddler-Example",
                                                             "Example java niddler application", "download"))
            .build()

    niddler.start()

    if (args.isNotEmpty()) {
        println("Waiting for debugger")
        niddler.debugger().waitForConnection {
            println("Debugger connected")
        }
    }

    val okHttp = OkHttpClient.Builder()
            .addInterceptor(NiddlerOkHttpInterceptor(niddler, "Default Interceptor"))
            .build()

    val request = Request.Builder()
            .get()
            .url("https://jsonplaceholder.typicode.com/posts")
            .build()
    val request2 = request.newBuilder().build()
    val request3 = Request.Builder()
            .url("http://httpbin.org/post")
            .post(FormBody.Builder().add("token", "add10 92201").add("type", "custom").build())
            .build()
    val request4 = Request.Builder()
            .get()
            .url(HttpUrl.parse("http://httpbin.org/xml")!!.newBuilder().addQueryParameter("param", "=102:~19em/%;").build())
            .build()
    val request5 = Request.Builder()
            .url("http://httpbin.org/post")
            .post(RequestBody.create(MediaType.parse("application/json"), "{\"nullValue\":null}"))
            .build()
    val request6 = Request.Builder()
            .url("http://httpbin.org/post")
            .post(RequestBody.create(MediaType.parse("application/octet-stream"), binaryBlob()))
            .build()

    val response1 = okHttp.newCall(request).execute()
    println("Request 1 executed (" + response1.code() + ")")
    val response2 = okHttp.newCall(request2).execute()
    println("Request 2 executed (" + response2.code() + ")")
    val response3 = okHttp.newCall(request3).execute()
    println("Request 3 executed (" + response3.code() + ")")
    val response4 = okHttp.newCall(request4).execute()
    println("Request 4 executed (" + response4.code() + ")")
    val response5 = okHttp.newCall(request5).execute()
    println("Request 5 executed (" + response5.code() + ")")
    val response6 = okHttp.newCall(request6).execute()
    println("Request 6 executed (" + response6.code() + ")")

    println("Press return to stop")
    System.`in`.read()

    println("Stopping niddler")
    niddler.close()
}

private fun binaryBlob(): ByteArray {
    val out = ByteArrayOutputStream()
    val zipOut = ZipOutputStream(out)
    zipOut.putNextEntry(ZipEntry("Example file 1"))

    val binaryBytes = ByteArray(100)
    Random().nextBytes(binaryBytes)

    zipOut.write(binaryBytes)

    zipOut.closeEntry()
    zipOut.close()

    return out.toByteArray()
}