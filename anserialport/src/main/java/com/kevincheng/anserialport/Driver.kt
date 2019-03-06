package com.kevincheng.anserialport

import com.orhanobut.logger.Logger
import java.io.File
import java.util.ArrayList

class Driver(val name: String, val root: String) {

    val devices: ArrayList<File>
        get() {
            val devices = ArrayList<File>()
            val dev = File("/dev")

            if (!dev.exists()) {
                Logger.e("${dev.absolutePath} doesn't exist")
                return devices
            }
            if (!dev.canRead()) {
                Logger.e("${dev.absolutePath} Permission denied")
                return devices
            }

            val files = dev.listFiles()
            for (file in files) {
                if (file.absolutePath.startsWith(root)) {
                    devices.add(file)
                }
            }

            return devices
        }
}