package com.example.keyapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.keyapp.data.AppDatabase
import com.example.keyapp.model.User
import kotlinx.coroutines.launch


class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _userExists = MutableLiveData<Boolean>()
    val userExists: LiveData<Boolean> get() = _userExists

    fun registerUser(username: String, password: String) {
        val user = User(username, password)
        viewModelScope.launch {
            userDao.insert(user)
            _user.postValue(user)
        }
    }

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.getUser(username, password)
            _user.postValue(user)
        }
    }

    fun checkUserExists() {
        viewModelScope.launch {
            val users = userDao.getAllUsers()
            _userExists.postValue(users.isNotEmpty())
        }
    }
}

