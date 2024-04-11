//package com.example.denso.local_database.repositiory
//
//
//import com.example.denso.dispatch.model.BinDispatchDetails
//import com.example.denso.dispatch.model.RfidTag
//import com.example.denso.local_database.LocalDataBaseDao
//
//class LocaleDataBaseRepository(private val localDataBaseDao: LocalDataBaseDao) {
//    suspend fun addDispatchResponsesInDb(binDispatchDetails: RfidTag) {
//        localDataBaseDao.addDispatchResponsesInDb(binDispatchDetails)
//    }
//
//
//    suspend fun getAllData(): List<RfidTag> {
//        return localDataBaseDao.getAllData()
//    }
//
//
//
//}