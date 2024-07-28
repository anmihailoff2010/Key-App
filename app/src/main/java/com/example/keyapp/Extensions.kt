package com.example.keyapp

fun ByteArray.toMacAddress(): String {
    return joinToString(":") { "%02X".format(it) }
}

