package com.yaxiu.bodycomposition

import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.vi.vioserial.util.SerialDataUtils
import com.yaxiu.bodycomposition.databinding.ActivityMainBinding
import com.yaxiu.bodycomposition.serial.CustomProber
import com.yaxiu.bodycomposition.serial.UsbSerialPortControl
import com.yaxiu.nativelib.NativeLib

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val serialPortControl = UsbSerialPortControl(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber: UsbSerialProber = CustomProber.customProber
        for (device in usbManager.deviceList.values) {
            var driver = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                for (port in driver.ports.indices) {
                    println(
                        "TAG refresh deviceId:" + device.deviceId + " deviceName:" + device.deviceName + " serialNumber:" + device.serialNumber + " productName:" + device.productName + " item.port:" + port
                                + " driver:" + driver.device.serialNumber
                    )

                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        serialPortControl.onResume()
    }

    override fun onPause() {
        serialPortControl.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        serialPortControl.onDestroy()
    }

    fun connect(view: View) {
        serialPortControl.connectBody()
    }


    fun disconnect(view: View) {

        serialPortControl.disconnect()
    }

    fun writeByte(view: View) {
        //  val content = "01 03 00 28 00 03 85 c3".toByteArray()
        val str = byteArrayOf(0x01, 0x03, 0x00, 0x28, 0x00, 0x03, 0x85.toByte(), 0xc3.toByte())

        serialPortControl.sentByteBody(str)
    }

    /**
     * 复位记录
     */
    fun resetRecord(view: View) {
        serialPortControl.resetRecord()
        val str = byteArrayOf(0x01, 0x03, 0x00, 0x28, 0x00, 0x03, 0x85.toByte(), 0xc3.toByte())
        val byteArrToHex = SerialDataUtils.ByteArrToHex(str)
        val cRC16 = NativeLib().cRC16(byteArrToHex, byteArrToHex.length)
        println("cRC16 = [${cRC16}] byteArrToHex=[$byteArrToHex]")
    }

    /**
     * 心率开机
     */
    fun openHeart(view: View) {
        serialPortControl.connectHeart()
    }

    /**
     * 心率关机
     */
    fun closeHeart(view: View) {
        serialPortControl.disconnect()
    }

    /**
     * 心率
     */
    fun heartRate(view: View) {
        serialPortControl.heartRate()
    }

    fun heartCmd(view: View) {
        serialPortControl.heartCmd()
    }


}