package com.yaxiu.bodycomposition.serial

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.util.*

/**
 * 心率usb通信
 */
class HeartUsbSerialPort(private val activity: Activity) : AbsSerialPort(activity) {

    private var deviceId: String = "7009"
    private var portNum: Int = 0
    private var baudRate: Int = 9600

    private var usbIoManager: SerialInputOutputManager? = null
    private var usbSerialPort: UsbSerialPort? = null
    private var connected = false

    override fun port(): String {
        return deviceId
    }

    override fun connect(vararg params: String) {
        Log.e(javaClass.simpleName, "TAG connect params:$params")
        if (connected) {
            return
        }
        deviceId = if (params.isNotEmpty()) {
            params[0]
        } else {
            "7009"
        }

        Log.e(javaClass.simpleName, "TAG connect devices:$deviceId port:$portNum baud:$baudRate")
        var device: UsbDevice? = null
        val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
        usbManager.deviceList.values.forEach {
            if (it.deviceId.toString() == deviceId) {
                device = it
            }
        }
        if (device == null) {
            Log.e(javaClass.simpleName, "TAG connection failed: device not found ")
            return
        }
        var probeDevice = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (probeDevice == null) {
            probeDevice = CustomProber.customProber.probeDevice(device)
        }
        if (probeDevice == null) {
            Log.e(javaClass.simpleName, "TAG connection failed: no driver for device")
            return
        }
        if (probeDevice.ports.size < portNum) {
            Log.e(javaClass.simpleName, "TAG connection failed: not enough ports at device")
            return
        }
        usbSerialPort = probeDevice.ports[portNum]
        val usbConnection = usbManager.openDevice(probeDevice.device)
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(
                probeDevice.device
            )
        ) {
            usbPermission = UsbPermission.Requested
            val usbPermissionIntent = PendingIntent.getBroadcast(
                activity,
                0,
                Intent(INTENT_ACTION_GRANT_USB),
                0
            )
            usbManager.requestPermission(probeDevice.device, usbPermissionIntent)
            return
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(probeDevice.device))
                Log.e(javaClass.simpleName, "TAG connection failed: permission denied")
            else Log.e(
                javaClass.simpleName,
                "TAG connection failed: open failed"
            )
            return
        }
        try {
            usbSerialPort?.apply {
                open(usbConnection)
                setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE)
                usbIoManager = SerialInputOutputManager(this)

                registerDataListener()
                usbIoManager?.start()
                connected = true
                /*mainLooper.postDelayed({
                    controlLines.forEach {
                        println("enum :$it")

                    }

                }, 200)*/
                Log.e(javaClass.simpleName, "TAG connected")

            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "TAG connection failed: " + e.message)
            disconnect()
        }
    }

    override fun disconnect() {
        if(isConnected()){
            usbIoManager?.apply {
                listener = null
                stop()

            }
            usbIoManager = null

            try {
                usbSerialPort?.close()
            } catch (ignored: Exception) {

            }
            usbSerialPort = null
        }
        connected = false


    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun sendData(data: String) {
        if (!connected) {
            Log.e(javaClass.simpleName, "TAG not connected")
            return
        }
        try {
            usbSerialPort?.write(
                data.toByteArray(),
                WRITE_WAIT_MILLIS
            )
        } catch (e: Exception) {
            onRunError(e)
        }
    }

    override fun sendData(data: ByteArray) {

    }

    override fun receiverData(): ByteArray {
        return mReceiverByte

    }

    override fun registerDataListener() {
        usbIoManager?.apply {
            listener = this@HeartUsbSerialPort
        }
    }

    override fun unregisterDataListener() {
        usbIoManager?.apply {
            listener = null
        }
    }


    override fun equals(other: Any?): Boolean {
        if (other is HeartUsbSerialPort) {
            return this.deviceId == other.deviceId

        }
        return super.equals(other)
    }

}
