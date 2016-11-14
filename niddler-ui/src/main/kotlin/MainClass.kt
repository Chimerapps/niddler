import com.icapps.niddler.ui.adb.ADBBootstrap

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */

fun main(args: Array<String>) {
    val adbConnection = ADBBootstrap().bootStrap()
    
    adbConnection.devices.forEach(::print)
}