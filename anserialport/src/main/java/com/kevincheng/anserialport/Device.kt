package com.kevincheng.anserialport

import java.io.File
import java.io.Serializable

class Device(val name: String, val root: String, val file: File) : Serializable