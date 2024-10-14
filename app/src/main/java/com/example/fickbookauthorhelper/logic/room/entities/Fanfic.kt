package com.example.fickbookauthorhelper.logic.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fanfic")
data class Fanfic(
    @PrimaryKey val id: Long,
    val name: String,
    val isHidden: Boolean,
    val isDone: Boolean,
)