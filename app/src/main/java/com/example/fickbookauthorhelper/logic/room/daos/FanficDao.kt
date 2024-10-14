package com.example.fickbookauthorhelper.logic.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fickbookauthorhelper.logic.room.entities.Fanfic

@Dao
interface FanficDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFanfic(fanfic: Fanfic)

    @Query("SELECT * FROM fanfic WHERE id = :id")
    suspend fun getFanficById(id: Int): Fanfic?

    @Query("SELECT * FROM fanfic")
    suspend fun getAllFanfics(): List<Fanfic>

    @Query("DELETE FROM fanfic WHERE id = :id")
    suspend fun deleteFanfic(id: Int)

    @Update
    suspend fun updateFanfic(fanfic: Fanfic)
}