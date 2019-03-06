package com.kevincheng.anserialportexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.orhanobut.logger.Logger
import java.io.IOException

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            // Get root access
            val su = Runtime.getRuntime().exec("su")
            // Make sure have read, write and execute permissions
            val cmd = "setprop service.adb.tcp.port 5555\nstop adbd\nstart adbd\nexit\n"
            su.outputStream.write(cmd.toByteArray())
            val result = "open adb service tcp port 5555 ${su.waitFor() == 0}"
            Toast.makeText(context, result, LENGTH_LONG).show()
            Logger.d(result)
        } catch (e: IOException) {
            // Don't have root access
            Logger.e("Get root access failed", e)
        } catch (e: InterruptedException) {
            Logger.e("Unexpected exception", e)
        }
    }
}