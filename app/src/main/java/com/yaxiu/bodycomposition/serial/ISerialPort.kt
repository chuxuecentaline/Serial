package com.yaxiu.bodycomposition.serial

/**
 * @author meicet
 * @date 2021/8/27 16:46
 * @modify 串口直接通信
 * @email
 */
interface ISerialPort {
    fun port(): String

    /**
     * 打开串口
     */
    fun connect(vararg params: String)

    /**
     * 重连
     */
    fun reconnect(vararg params: String)

    /**
     * 关闭
     */
    fun disconnect()

    /**
     * 是否连接
     */
    fun isConnected(): Boolean


    /**
     * 发送数据
     */
    fun sendData(data: String)

    /**
     * 发送数据
     */
    fun sendData(data: ByteArray)

    /**
     * 获取数据
     */
    fun receiverData(): ByteArray

    /**
     * 通信
     */
    fun registerDataListener()

    /**
     * 移除通信
     */
    fun unregisterDataListener()


}