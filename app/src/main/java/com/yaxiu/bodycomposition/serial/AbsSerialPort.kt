package com.yaxiu.bodycomposition.serial

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.vi.vioserial.util.SerialDataUtils
import com.yaxiu.bodycomposition.BuildConfig
import com.yaxiu.bodycomposition.util.HexDump

/**
 * @author meicet
 * @date 2021/8/24 16:05
 * @modify 串口连接
 * @email
 */
abstract class AbsSerialPort(private val activity: Activity) : ISerialPort, AbsSerialLife,
    SerialInputOutputManager.Listener {

    companion object {
        const val INTENT_ACTION_GRANT_USB: String =
            "${BuildConfig.APPLICATION_ID}.GRANT_USB"
        const val WRITE_WAIT_MILLIS = 2000
        const val READ_WAIT_MILLIS = 2000
        const val TAG="TAG"
    }

    protected var mReceiverByte: ByteArray = ByteArray(1)
    var usbPermission = UsbPermission.Unknown
    val mainLooper = Handler(Looper.getMainLooper())

    private val broadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(javaClass.simpleName, "TAG onReceive :${intent.action}")
                if (INTENT_ACTION_GRANT_USB == intent.action) {
                    usbPermission = if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED,
                            false
                        )
                    ) UsbPermission.Granted else UsbPermission.Denied
                    connect(port())
                }
            }
        }


    override fun onResume() {
        Log.i(javaClass.simpleName,  "TAG onResume")

        activity.registerReceiver(
            broadcastReceiver,
            IntentFilter(INTENT_ACTION_GRANT_USB)
        )

    }

    override fun reconnect(vararg params: String) {
        if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted)
            mainLooper.post {
                connect(port())
            }

    }

    override fun onPause() {
       Log.i(javaClass.simpleName,  "TAG onPause")
        activity.unregisterReceiver(broadcastReceiver)
    }


    override fun onNewData(data: ByteArray) {
        mainLooper.post {
            if (data.isNotEmpty()) {
                val dumpHexString = SerialDataUtils.ByteArrToHex(data)

              //  val hexToInt = SerialDataUtils.HexToInt(dumpHexString)
                Log.i(javaClass.simpleName,  "TAG receive: $dumpHexString hexToInt:$1")
                mReceiverByte = data
            }

        }
    }

    override fun onRunError(e: Exception?) {
        mainLooper.post {
            Log.e(javaClass.simpleName, "TAG connection lost: " + e?.message)
            disconnect()
        }
    }


}

interface AbsSerialLife {
    fun onResume()
    fun onPause()

}

enum class UsbPermission {
    Unknown, Requested, Granted, Denied
}

