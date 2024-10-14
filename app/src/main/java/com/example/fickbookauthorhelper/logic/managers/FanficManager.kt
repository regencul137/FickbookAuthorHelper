package com.example.fickbookauthorhelper.logic.managers

import com.example.fickbookauthorhelper.logic.room.daos.FanficDao
import com.example.fickbookauthorhelper.logic.room.entities.Fanfic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FanficManager @Inject constructor(private val fanficsDao: FanficDao) {
    suspend fun addFanfic(fanfic: Fanfic) {
        fanficsDao.insertFanfic(fanfic)
    }

    suspend fun getFanficById(id: Int): Fanfic? {
        return fanficsDao.getFanficById(id)
    }

    suspend fun getAllFanfics(): List<Fanfic> {
        return fanficsDao.getAllFanfics()
    }

    suspend fun deleteFanfic(id: Int) {
        fanficsDao.deleteFanfic(id)
    }

    suspend fun updateFanfic(fanfic: Fanfic) {
        fanficsDao.updateFanfic(fanfic)
    }
}