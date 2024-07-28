package com.example.keyapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.*
import com.example.keyapp.NfcManager
import com.example.keyapp.data.AppDatabase
import com.example.keyapp.model.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(context: Context) : ViewModel() {

    private val nfcManager = NfcManager(context)
    private val keyDao = AppDatabase.getDatabase(context).keyDao()
    private val _readKeyLiveData = MutableLiveData<ByteArray?>()
    val readKeyLiveData: LiveData<ByteArray?> get() = _readKeyLiveData
    private val _allKeysLiveData = MutableLiveData<List<Key>>()
    val allKeysLiveData: LiveData<List<Key>> get() = _allKeysLiveData

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("com.example.keyapp.prefs", Context.MODE_PRIVATE)
    private var cachedUID: ByteArray? = null

    fun processTag(tag: Tag) {
        val key = nfcManager.readUID(tag)
        Log.d("MainViewModel", "Read key: ${key?.toHexString()}")
        _readKeyLiveData.postValue(key)
        if (key != null) {
            saveKey(Key(keyData = key))
            storeUID(key)
        }
    }

    private fun saveKey(key: Key) {
        viewModelScope.launch(Dispatchers.IO) {
            keyDao.insert(key)
            _allKeysLiveData.postValue(keyDao.getAllKeys())
        }
    }

    fun getAllKeys(): LiveData<List<Key>> {
        viewModelScope.launch(Dispatchers.IO) {
            _allKeysLiveData.postValue(keyDao.getAllKeys())
        }
        return allKeysLiveData
    }

    fun deleteKey(key: Key) {
        viewModelScope.launch(Dispatchers.IO) {
            keyDao.delete(key)
            _allKeysLiveData.postValue(keyDao.getAllKeys())
        }
    }

    fun openLock(key: ByteArray): Boolean {
        Log.d("MainViewModel", "Opening lock with key: ${key.toHexString()}")
        val isValid = isKeyValid(key)
        Log.d("MainViewModel", "Key validity check returned: $isValid")
        if (isValid) {
            Log.d("MainViewModel", "Attempting to communicate with lock hardware")
            val lockOpened = retryOpenLockHardware()
            Log.d("MainViewModel", "Lock opened: $lockOpened")
            return lockOpened
        }
        return false
    }

    private fun retryOpenLockHardware(retries: Int = 3, delayMs: Long = 500): Boolean {
        repeat(retries) { attempt ->
            Log.d("NfcManager", "Sending open command to lock hardware, attempt ${attempt + 1}")
            if (nfcManager.openLockHardware()) {
                Log.d("NfcManager", "Lock hardware confirmed opening on attempt ${attempt + 1}")
                return true
            }
            Thread.sleep(delayMs)
        }
        Log.w("NfcManager", "Failed to open lock after $retries attempts")
        return false
    }

    fun writeKeyToExternalReader(key: ByteArray): Boolean {
        return nfcManager.writeKeyToExternalReader(key)
    }

    private fun storeUID(uid: ByteArray) {
        val uidString = uid.toHexString()
        with(sharedPreferences.edit()) {
            putString("saved_uid", uidString)
            apply()
        }
        cachedUID = uid
        Log.d("MainViewModel", "Storing UID: $uidString")
    }

    fun loadSavedUID(): ByteArray? {
        if (cachedUID == null) {
            val uidString = sharedPreferences.getString("saved_uid", null)
            cachedUID = uidString?.hexStringToByteArray()
        }
        Log.d("MainViewModel", "Loaded UID: ${cachedUID?.toHexString()}")
        return cachedUID
    }

    fun isKeyValid(key: ByteArray): Boolean {
        val savedUID = loadSavedUID()
        val isValid = savedUID?.contentEquals(key) == true
        Log.d("MainViewModel", "Checking key validity: ${key.toHexString()} vs ${savedUID?.toHexString()}, isValid: $isValid")
        return isValid
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val hexChars = "0123456789abcdef"
        val result = ByteArray(length / 2)
        for (i in indices step 2) {
            val firstIndex = hexChars.indexOf(this[i])
            val secondIndex = hexChars.indexOf(this[i + 1])
            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toByte()
        }
        return result
    }
}

