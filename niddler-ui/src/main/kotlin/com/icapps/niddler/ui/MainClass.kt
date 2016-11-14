package com.icapps.niddler.ui

import com.icapps.niddler.ui.adb.ADBBootstrap
import trikita.log.Log
import java.net.URI

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun main(args: Array<String>) {
    val adbBootstrap = ADBBootstrap()
    val adbConnection = adbBootstrap.bootStrap()
    adbBootstrap.extend(adbConnection.anyDevice).fowardTCPPort(6555, 6555)

    Log.d("Connecting to localhost:6555 ...")
    val client = NiddlerClient(URI.create("ws://127.0.0.1:6555"))
    client.connect()

    while (!client.connection.isOpen) {
        Log.d("Trying to connect...")
        Thread.sleep(1000)
    }

    while (!client.connection.isClosed) {
    }
}
