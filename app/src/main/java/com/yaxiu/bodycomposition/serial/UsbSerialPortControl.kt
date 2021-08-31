package com.yaxiu.bodycomposition.serial

import android.annotation.SuppressLint
import com.vi.vioserial.util.SerialDataUtils
import com.yaxiu.bodycomposition.MainActivity
import java.lang.Exception
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author meicet
 * @date 2021/8/24 16:43
 * @modify
 * @email
 */
class UsbSerialPortControl(activity: MainActivity) {


    private lateinit var timer: Timer
    private var secondTimer = 60
    private val bodyUsbSerialPort = BodyUsbSerialPort(activity)
    private val heartSubSerialPort = HeartUsbSerialPort(activity)
    private val bodySerialPort = BodySerialPort()
    private val heartSerialPort = HeartSerialPort()
    private val connectPorts = ArrayList<ISerialPort>()

    fun connectBody() {
        bodySerialPort.connect("/dev/ttyAMA3")
        if (!connectPorts.contains(bodySerialPort)) {
            connectPorts.add(bodySerialPort)
        }

    }

    fun connectHeart() {
        heartSubSerialPort.connect("5003")
        if (!connectPorts.contains(heartSubSerialPort)) {
            connectPorts.add(heartSubSerialPort)
        }

    }

    fun resetRecord() {

    }

    fun sentByteBody(data: ByteArray) {

        bodySerialPort.sendData(data)

    }

    @SuppressLint("SoonBlockedPrivateApi")
    fun heartRate() {
        secondTimer = 60
        timer = Timer()
        try {
            val field: Field = TimerTask::class.java.getDeclaredField("state")
            field.isAccessible = true
            field[timerTask] = 0

        } catch (e: NoSuchFieldException) {

            e.printStackTrace()
        } catch (e: Exception) {

            e.printStackTrace()
        }
        timer.schedule(
            timerTask,
            0,
            100
        )

    }

    private var timerTask: TimerTask = object : TimerTask() {
        var effectiveValueCount = 0
        var effectiveValue = 0
        override fun run() {
            if (secondTimer == 0) {
                cancel()
                if (effectiveValueCount != 0) {
                    val hearReteValue = effectiveValue / effectiveValueCount
                    println("SerialPortControl TAG receiverData:${hearReteValue}")

                }
                return
            }
            val receiverData = heartSubSerialPort.receiverData()
            val byteArrToHex = SerialDataUtils.ByteArrToHex(receiverData)
            val split = byteArrToHex.split(" ")
            println("SerialPortControl TAG split:${split} secondTimer:$secondTimer")
            if (split.isNotEmpty()) {
                val data = split[split.size - 2]
                if (data.isNotEmpty()) {
                    val hexToInt = SerialDataUtils.HexToInt(data)
                    println("SerialPortControl TAG hexToInt:${hexToInt}")
                    if (hexToInt > 0) {
                        effectiveValueCount++
                        effectiveValue += hexToInt
                    }
                }

            }

            secondTimer--
        }
    }

    fun disconnect() {
        connectPorts.forEach {
            it.disconnect()
        }

        println("SerialPortControl TAG disconnect:${connectPorts.size}")

    }

    fun onPause() {
        connectPorts.forEach {
            it.unregisterDataListener()
            it.disconnect()
        }
        bodyUsbSerialPort.onPause()
        println("SerialPortControl TAG onPause:${connectPorts.size}")
    }

    fun onResume() {
        bodyUsbSerialPort.onResume()
        connectPorts.forEach {
            it.reconnect(it.port())
        }
        println("SerialPortControl TAG onResume:${connectPorts.size}")
    }

    /**
     * 主板To MCU命令表： 头码（0x55,0xAA）+ 命令码 (1-Byte):
     * 	                 头码	    命令码
     *   字节序号	Byte.1	Byte.2	Byte.3
     *   开机	     0x55	0xAA	0xA5
     *   复位记录	 0x55	0xAA	0xA0
     *   关机	     0x55	0xAA	0x00
     */
    fun heartCmd() {
        //0x55	0xAA	0xA5
        //0x55	0xAA	0x00
        val content = "55 AA A0"
        heartSubSerialPort.sendData(content)

    }

    fun onDestroy() {
        connectPorts.clear()
        if (::timer.isInitialized) {
            timer.cancel()
        }
    }
}