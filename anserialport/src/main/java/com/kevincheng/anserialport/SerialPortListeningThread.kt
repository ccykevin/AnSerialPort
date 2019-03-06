package com.kevincheng.anserialport

import com.orhanobut.logger.Logger
import java.io.IOException
import java.io.InputStream

class SerialPortListeningThread(private val mInputStream: InputStream, bufferSize: Int, val onDataReceived: (bytes: ByteArray) -> Unit) : Thread() {
    private val mReadBuffer: ByteArray = ByteArray(bufferSize)

    override fun run() {
        super.run()

        while (!isInterrupted) {
            try {
                val size = mInputStream.read(mReadBuffer)
                when {
                    size != -1 && size > 0 -> {
                        val readBytes = ByteArray(size)
                        System.arraycopy(mReadBuffer, 0, readBytes, 0, size)
                        onDataReceived(readBytes)
                    }
                }
            } catch (e: IOException) {
                Logger.e("Unexpected Exception", e)
            }
        }
    }

    /** Close the thread and release related resources */
    fun release() {
        interrupt()
        try {
            mInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}