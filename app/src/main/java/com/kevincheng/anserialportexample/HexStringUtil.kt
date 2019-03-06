package com.kevincheng.anserialportexample

val ByteArray.asHexLower inline get() = this.joinToString(separator = "") { String.format("%02x", (it.toInt() and 0xFF)) }
val ByteArray.asHexUpper inline get() = this.joinToString(separator = "") { String.format("%02X", (it.toInt() and 0xFF)) }
val String.asHexUpperToByteArray inline get() = this.map { String.format("%02X", (it.toInt() and 0xFF)) }.joinToString("").toByteArray()
val String.hexAsByteArray inline get() = this.chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()
val String.tryHexAsByteArray
    inline get() = try {
        this.chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()
    } catch (e: Throwable) {
        null
    }