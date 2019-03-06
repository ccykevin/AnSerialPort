package com.kevincheng.anserialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Process.*
import com.orhanobut.logger.Logger
import java.io.*
import java.lang.ref.WeakReference

class AnSerialPort(val device: File, val baudRate: Int, private val readBufferSize: Int, listener: Listener?) : SerialPort() {
    private var fileDescriptor: FileDescriptor? = null
    private var outputStream: FileOutputStream? = null
    private var serialPortListeningThread: SerialPortListeningThread? = null
    private var serialPortSendingThread: HandlerThread? = null
    private var serialPortSendingHandler: Handler? = null
    private val listenerWeakReference: WeakReference<Listener>? = when {
        listener != null -> WeakReference(listener)
        else -> null
    }

    fun open(): Boolean {
        when {
            fileDescriptor != null -> close()
        }

        if (!device.canRead() || !device.canWrite()) {
            val chmod777 = chmod777(device)
            if (!chmod777) {
                listenerWeakReference?.get()?.onFailOpen(device, Listener.Status.PERMISSION_DENIED)
                return false
            }
        }

        try {
            fileDescriptor = nativeOpen(device.absolutePath, baudRate, 0)
            outputStream = FileOutputStream(fileDescriptor)
            startSendThread()
            startListeningThread(FileInputStream(fileDescriptor))
            listenerWeakReference?.get()?.onSuccessOpened(device)
            return true
        } catch (e: Exception) {
            Logger.d("Unexpected Exception", e)
            listenerWeakReference?.get()?.onFailOpen(device, Listener.Status.UNKNOWN)
        }

        return false
    }

    fun close() {
        when {
            fileDescriptor != null -> {
                nativeClose()
                fileDescriptor = null
                stopSendThread()
                stopListeningThread()
            }
        }
    }

    fun sendBytes(sendBytes: ByteArray): Boolean = when {
        fileDescriptor != null && outputStream != null && serialPortSendingHandler != null -> {
            serialPortSendingHandler!!.post(SendBytesRunnable(this, sendBytes))
            true
        }
        else -> false
    }

    //================================================================================
    // Runnable
    //================================================================================

    class SendBytesRunnable(anSerialPort: AnSerialPort, private val bytes: ByteArray) : Runnable {
        private val anSerialPortWeakReference: WeakReference<AnSerialPort> = WeakReference(anSerialPort)

        override fun run() {
            val anSerialPort = anSerialPortWeakReference.get() ?: return
            if (anSerialPort.fileDescriptor != null) {
                try {
                    anSerialPort.outputStream?.write(bytes)
                    anSerialPort.listenerWeakReference?.get()?.onDataSent(bytes)
                } catch (e: IOException) {
                    Logger.e("Unexpected Exception", e)
                }
            }
        }
    }

    //================================================================================
    // Sending Thread
    //================================================================================

    private fun startSendThread() {
        serialPortSendingThread = HandlerThread("AnSerialPort-SendingHandlerThread", THREAD_PRIORITY_BACKGROUND)
        serialPortSendingThread!!.start()
        serialPortSendingHandler = Handler(serialPortSendingThread!!.looper)
    }

    private fun stopSendThread() {
        serialPortSendingHandler?.removeCallbacksAndMessages(null)
        serialPortSendingThread?.interrupt()
        try {
            outputStream?.close()
        } catch (e: IOException) {
            Logger.e("Unexpected Exception", e)
        }
        serialPortSendingThread?.quit()
        serialPortSendingThread = null
    }

    //================================================================================
    // Listening Thread
    //================================================================================

    private fun startListeningThread(inputStream: InputStream) {
        val weakSelf: WeakReference<AnSerialPort> = WeakReference(this)
        serialPortListeningThread = SerialPortListeningThread(inputStream, readBufferSize) {
            val self = weakSelf.get() ?: return@SerialPortListeningThread
            when {
                self.fileDescriptor != null -> self.listenerWeakReference?.get()?.onDataReceived(it)
            }
        }
        serialPortListeningThread?.start()
    }

    private fun stopListeningThread() {
        serialPortListeningThread?.release()
        serialPortListeningThread = null
    }

    //================================================================================
    // Listener
    //================================================================================

    interface Listener {
        enum class Status {
            PERMISSION_DENIED, UNKNOWN
        }

        fun onSuccessOpened(device: File)

        fun onFailOpen(device: File, status: Status)

        fun onDataReceived(bytes: ByteArray)

        fun onDataSent(bytes: ByteArray)
    }
}