package com.kevincheng.anserialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class SerialPort {

    static {
        System.loadLibrary("serial_port");
    }

    private static final String TAG = SerialPort.class.getSimpleName();

    boolean chmod777(File file) {
        if (null == file || !file.exists()) {
            // Port doesn't exists
            return false;
        }
        try {
            // Get root access
            Process su = Runtime.getRuntime().exec("/system/bin/su");
            // Make sure have read, write and execute permissions
            String cmd = "chmod 777 " + file.getAbsolutePath() + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if (0 == su.waitFor() && file.canRead() && file.canWrite() && file.canExecute()) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            // Don't have root access
            e.printStackTrace();
        }
        return false;
    }

    // Open serial port
    protected native FileDescriptor nativeOpen(String path, int baudRate, int flags);

    // Close serial port
    protected native void nativeClose();
}