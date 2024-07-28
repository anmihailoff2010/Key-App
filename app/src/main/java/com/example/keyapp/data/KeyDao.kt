package com.example.keyapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.keyapp.model.Key

@Dao
interface KeyDao {
    @Insert
    suspend fun insert(key: Key)

    @Query("SELECT * FROM keys")
    suspend fun getAllKeys(): List<Key>

    @Delete
    suspend fun delete(key: Key)
}

