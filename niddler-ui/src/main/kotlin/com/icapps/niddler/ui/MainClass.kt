package com.icapps.niddler.ui

import com.icapps.niddler.ui.adb.ADBInterface
import java.net.URI

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
fun main(args: Array<String>) {
    val adbConnection = ADBInterface()
    adbConnection.bootStrap()
    adbConnection.forwardPort(6555, 6555)

    print("Connecting to localhost:6555 ...")
    val client = NiddlerClient(URI.create("http://localhost:6555"))
    client.connectBlocking()

    while (!client.connection.isOpen) {
    }

    print("Connected!")

    while (client.connection.isOpen) {
        Thread.sleep(100);
    }
}
