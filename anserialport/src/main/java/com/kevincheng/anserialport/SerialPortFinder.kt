package com.kevincheng.anserialport

import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader

class SerialPortFinder {
    companion object {
        const val DRIVERS_PATH = "/proc/tty/drivers"
        const val SERIAL_FIELD = "serial"
    }

    init {
        val file = File(DRIVERS_PATH)
        when {
            !file.canRead() -> Logger.e("Permission denied")
        }
    }

    @Throws(IOException::class)
    private fun getDrivers(): ArrayList<Driver> {
        val drivers: ArrayList<Driver> = arrayListOf()
        val lineNumberReader = LineNumberReader(FileReader(DRIVERS_PATH))
        lineNumberReader.forEachLine {
            val driverName = it.substring(0, 0x15).trim { it <= ' ' }
            val fields = it.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // Filter non-serial port driver
            if (fields.size >= 5 && fields[fields.size - 1] == SERIAL_FIELD) {
                drivers.add(Driver(driverName, fields[fields.size - 4]))
            }
        }

        return drivers
    }

    fun getDevices(): ArrayList<Device> {
        val devices: ArrayList<Device> = arrayListOf()
        try {
            val drivers = getDrivers()
            for (driver in drivers) {
                val driverDevices = driver.devices
                for (file in driverDevices) {
                    val devicesName = file.name
                    devices.add(Device(devicesName, driver.root, file))
                }
            }
        } catch (e: IOException) {
            Logger.e("Unexpected Exception", e)
        }

        return devices
    }
}