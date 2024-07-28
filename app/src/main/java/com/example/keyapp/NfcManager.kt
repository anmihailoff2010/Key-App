package com.example.keyapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import java.io.IOException
import java.util.Random
import java.util.UUID


class NfcManager(private val context: Context) {

    private var storedUID: ByteArray? = null

    fun readUID(tag: Tag): ByteArray? {
        val mifareClassic = MifareClassic.get(tag)
        return if (mifareClassic != null) {
            try {
                mifareClassic.connect()
                val id = mifareClassic.tag.id
                Log.d("NfcManager", "Read UID: ${id.joinToString(",")}")
                id
            } catch (e: IOException) {
                Log.e("NfcManager", "Error reading MIFARE tag UID", e)
                null
            } finally {
                try {
                    mifareClassic.close()
                } catch (e: IOException) {
                    Log.e("NfcManager", "Error closing MIFARE tag connection", e)
                }
            }
        } else {
            val id = tag.id
            Log.d("NfcManager", "Read UID: ${id.joinToString(",")}")
            id
        }
    }

    fun isKeyValid(key: ByteArray): Boolean {
        val validKey = getSavedUID()
        val isValid = validKey?.contentEquals(key) ?: false
        Log.d("NfcManager", "Checking key validity: ${key.joinToString(",")} vs ${validKey?.joinToString(",")}, isValid: $isValid")
        return isValid
    }

    fun getSavedUID(): ByteArray? {
        Log.d("NfcManager", "Getting saved UID: ${storedUID?.joinToString(",")}")
        return storedUID
    }

    fun storeUID(uid: ByteArray) {
        Log.d("NfcManager", "Storing UID: ${uid.joinToString(",")}")
        storedUID = uid
    }

    fun writeKeyToExternalReader(key: ByteArray): Boolean {
        // Logic to write key to external reader
        Log.d("NfcManager", "Writing key to external reader: ${key.joinToString(",")}")
        return true
    }

    fun openLockHardware(): Boolean {
        Log.d("NfcManager", "Sending open command to lock hardware")
        return try {
            val command = generateOpenCommand()
            Log.d("NfcManager", "Command sent to hardware: $command")
            val response = sendCommandToHardware(command)
            Log.d("NfcManager", "Response from hardware: $response")
            response == "SUCCESS"
        } catch (e: Exception) {
            Log.e("NfcManager", "Error communicating with lock hardware", e)
            false
        }
    }

    // Simulated functions for example
    fun generateOpenCommand(): String {
        return "OPEN_LOCK"
    }

    fun sendCommandToHardware(command: String): String {
        // This function should interact with the actual lock hardware
        // Simulate successful response
        return "SUCCESS"
    }
}

