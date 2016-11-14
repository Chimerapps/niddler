package com.icapps.niddler.ui.adb

import se.vidstige.jadb.JadbDevice

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class ADBExt(private val adbDevice: JadbDevice, private val adbBootstrap: ADBBootstrap) {

    fun fowardTCPPort(localPort: Int, remotePort: Int) {
        if (adbDevice.serial != null)
            adbBootstrap.executeADBCommand("-s", adbDevice.serial, "forward", "tcp:$localPort", "tcp:$remotePort")
        else
            adbBootstrap.executeADBCommand("forward", "tcp:$localPort", "tcp:$remotePort")
    }

}