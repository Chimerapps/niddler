package com.icapps.niddler.ui.adb

import com.icapps.niddler.ui.prefixList
import java.io.File

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
class ADBInterface {

    companion object {
        private fun determineExecutablePath(name: String): String? {
            val foundPath = System.getenv("PATH").split(File.pathSeparator).find {
                val file = File(it, name)
                file.exists() && file.canExecute()
            }
            if (foundPath != null)
                return "$foundPath${File.separator}$name"
            return null
        }
    }

    private var pathToAdb: String? = determineExecutablePath("adb")

    fun hasADB(): Boolean {
        return pathToAdb != null
    }

    fun test() {
        executeADBCommand("devices")
    }

    private fun executeADBCommand(vararg commands: String) {
        val builder = ProcessBuilder(pathToAdb?.prefixList(commands))
        val process = builder.start()
        process.waitFor()
        print(process.inputStream.bufferedReader().readText())
        System.err.println(process.errorStream.bufferedReader().readText())
    }

}
