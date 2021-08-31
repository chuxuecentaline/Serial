package com.yaxiu.bodycomposition.serial

import android.util.Log
import com.vi.vioserial.NormalSerial
import com.vi.vioserial.listener.OnNormalDataListener

/**
 * @author meicet
 * @date 2021/8/30 10:32
 * @modify 心率串口通信
 * @email
 */
class HeartSerialPort : ISerialPort, OnNormalDataListener {

    private var portStr: String = ""
    private var mHexData: String = ""
    private var openStatus: Int = -1
    override fun port(): String {
        return portStr
    }

    override fun connect(vararg params: String) {
        Log.i(javaClass.simpleName, "TAG connect params:$params")
        portStr = if (params.isNotEmpty()) {
            params[0]
        } else ""
        if (openStatus == -1) {
            openStatus = NormalSerial.instance().open(portStr, 9600)
            //添加数据接收回调 Add data receive callback
            if (isConnected()) {
                registerDataListener()
            }
            Log.i(javaClass.simpleName, "TAG openStatus:$openStatus")
        }

    }

    override fun reconnect(vararg params: String) {
        connect(*params)
    }

    override fun disconnect() {
        Log.i(javaClass.simpleName, "TAG disconnect")
        NormalSerial.instance().close()
    }

    override fun isConnected(): Boolean {
        return openStatus == 0
    }

    override fun sendData(data: String) {
        if (isConnected()) {
            Log.i(javaClass.simpleName, "TAG sendData:$data")
            NormalSerial.instance().sendHex(data)
        }

    }

    override fun sendData(data: ByteArray) {

    }

    override fun receiverData(): ByteArray {
        return mHexData.toByteArray()
    }

    override fun registerDataListener() {
        Log.i(javaClass.simpleName, "TAG registerDataListener")
        NormalSerial.instance().addDataListener(this)
    }

    override fun unregisterDataListener() {
        Log.i(javaClass.simpleName, "TAG unregisterDataListener")
        NormalSerial.instance().removeDataListener(this)
        NormalSerial.instance().close()
    }

    override fun normalDataBack(hexData: String) {
        Log.i(javaClass.simpleName, "TAG normalDataBack:$mHexData")
        mHexData = hexData
    }

    override fun equals(other: Any?): Boolean {
        if (other is HeartSerialPort) {
            return this.portStr == other.portStr

        }
        return super.equals(other)
    }
}