package com.kevincheng.anserialportexample

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.HandlerThread
import android.support.multidex.MultiDex
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class AnSerialPortExampleApplication : Application(), Thread.UncaughtExceptionHandler {
    private val uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        val mLoggerHandlerThread = HandlerThread("SerialPortManagerExampleApplication-Logger")
        mLoggerHandlerThread.start()
        Logger.addLogAdapter(AndroidLogAdapter())
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        Logger.e(e, "CRASH LOG")
        uncaughtExceptionHandler.uncaughtException(t, e)
    }
}