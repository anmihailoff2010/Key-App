package com.example.keyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockViewModel : ViewModel() {

    private val _lockStatus = MutableLiveData<String>("Lock is closed")
    val lockStatus: LiveData<String> get() = _lockStatus

    fun openLock() {
        viewModelScope.launch(Dispatchers.IO) {
            // Имитация открытия замка (здесь будет ваша логика работы с NFC)
            val success = simulateLockOperation()

            // Обновление состояния замка на главном потоке
            _lockStatus.postValue(if (success) "Lock opened successfully!" else "Failed to open lock.")
        }
    }

    private fun simulateLockOperation(): Boolean {
        // Симуляция задержки и успешного открытия замка
        Thread.sleep(1000)
        return true
    }
}

