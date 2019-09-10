package com.kevincheng.anserialportexample

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import com.kevincheng.anserialport.*
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), AnSerialPort.Listener {

    private var isOpened: Boolean = false
    private var androidSerialPort: AnSerialPort? = null
    private var devices: ArrayList<Device> = arrayListOf()
    private val baudRates = arrayListOf("0", "50", "75", "110", "134", "150", "200", "300", "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000", "2500000", "3000000", "3500000", "4000000")
    private val outgoingMessages: ArrayList<Message> = arrayListOf()
    private val outgoingMessagesAdapter: MessagesDataAdapter = MessagesDataAdapter(outgoingMessages, Color.parseColor("#4BB543"))
    private val incomingMessages: ArrayList<Message> = arrayListOf()
    private val incomingMessagesAdapter: MessagesDataAdapter = MessagesDataAdapter(incomingMessages, Color.parseColor("#FF3E82"))
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView_outgoing_message.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_outgoing_message.adapter = outgoingMessagesAdapter
        recyclerView_incoming_message.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_incoming_message.adapter = incomingMessagesAdapter

        val serialPortFinder = SerialPortFinder()
        devices = serialPortFinder.getDevices()
        val devicesName = devices.map { it.name }
        spinner_serial_port.adapter = SpinnerAdapter(this, devicesName)

        spinner_baud_rate.adapter = SpinnerAdapter(this, baudRates)

        button_enable_port.setOnClickListener {
            if (isOpened) {
                androidSerialPort?.close()
                isOpened = false
                outgoingMessages.clear()
                incomingMessages.clear()
                runOnUiThread {
                    outgoingMessagesAdapter.notifyDataSetChanged()
                    incomingMessagesAdapter.notifyDataSetChanged()
                }
            } else {
                val device = devices[spinner_serial_port.selectedItemPosition]
                val baudRate = baudRates[spinner_baud_rate.selectedItemPosition].toInt()

                androidSerialPort = AnSerialPort(device.file, baudRate, 1024, this)

                isOpened = androidSerialPort!!.open()
            }
            (it as Button).text = when (isOpened) {
                true -> "CLOSE"
                false -> "OPEN"
            }
            button_sent_message.isEnabled = isOpened
            spinner_serial_port.isEnabled = !isOpened
            spinner_baud_rate.isEnabled = !isOpened
        }

        button_sent_message.setOnClickListener {
            if (!isOpened) return@setOnClickListener
            val value = editText_message.text.toString()
            if (value.isEmpty()) return@setOnClickListener
            if (radioButton_message_type_string.isChecked) {
                androidSerialPort?.sendBytes(value.toByteArray())
            } else if (radioButton_message_type_hex_string.isChecked) {
                val bytes = value.asHexUpperToByteArray
                androidSerialPort?.sendBytes(bytes)
            }
        }
    }

    override fun onSuccessOpened(device: File) {
        Logger.d(device.absolutePath)
    }

    override fun onFailOpen(device: File, status: AnSerialPort.Listener.Status) {
        Logger.d("${device.absolutePath} $status")
    }

    override fun onDataReceived(bytes: ByteArray) {
        Logger.d(String(bytes))
        incomingMessages.add(Message(dateFormatter.format(Date()), bytes))
        runOnUiThread {
            incomingMessagesAdapter.notifyDataSetChanged()
            recyclerView_incoming_message.smoothScrollToPosition(incomingMessages.size)
        }
    }

    override fun onDataSent(bytes: ByteArray) {
        Logger.d(String(bytes))
        outgoingMessages.add(Message(dateFormatter.format(Date()), bytes))
        runOnUiThread {
            outgoingMessagesAdapter.notifyDataSetChanged()
            recyclerView_outgoing_message.smoothScrollToPosition(outgoingMessages.size)
        }
    }
}