package com.icapps.niddler.ui.adb

/**
 * @author Nicola Verbeeck
 * @date 14/11/16.
 */
class ADBExt(private val serial: String?, private val adbBootstrap: ADBBootstrap) {

    fun forwardTCPPort(localPort: Int, remotePort: Int) {
        if (serial != null)
            adbBootstrap.executeADBCommand("-s", serial, "forward", "tcp:$localPort", "tcp:$remotePort")
        else
            adbBootstrap.executeADBCommand("forward", "tcp:$localPort", "tcp:$remotePort")
    }

}