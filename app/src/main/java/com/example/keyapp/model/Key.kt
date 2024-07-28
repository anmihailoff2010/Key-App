package com.example.keyapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keys")
data class Key(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyData: ByteArray
)

