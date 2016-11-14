package com.icapps.niddler.ui.adb

import com.icapps.niddler.ui.prefixList
import java.io.File

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
class ADBInterface {

    companion object {
        private fun determineExecutablePath(name: String): String {
            val foundPath = System.getenv("PATH").split(File.pathSeparator).find {
                val file = File(it, name)
                file.exists() && file.canExecute()
            }
            if (foundPath != null)
                return "$foundPath${File.separator}$name"
            throw IllegalStateException("Failed to find adb")
        }

        private fun findADB(): String {
            val androidHomePath = System.getenv("ANDROID_HOME")
            if (androidHomePath != null) {
                val adbFile = File("$androidHomePath${File.separator}platform-tools/adb")
                if (adbFile.exists() && adbFile.canExecute())
                    return adbFile.absolutePath
            }
            return determineExecutablePath("adb")
        }
    }

    private var pathToAdb: String = findADB()

    fun bootStrap() {
        executeADBCommand("start-server")
    }

    fun forwardPort(localPort: Int, remotePort: Int) {
        executeADBCommand("forward", "tcp:$localPort", "tcp:$remotePort")
    }

    private fun executeADBCommand(vararg commands: String) {
        val builder = ProcessBuilder(pathToAdb.prefixList(commands))
        val process = builder.start()
        process.waitFor()
        print(process.inputStream.bufferedReader().readText())
        System.err.println(process.errorStream.bufferedReader().readText())
    }

}
