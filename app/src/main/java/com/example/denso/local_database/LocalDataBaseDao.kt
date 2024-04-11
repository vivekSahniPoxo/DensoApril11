//package com.example.denso.local_database
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//
//import com.example.denso.dispatch.model.BinDispatchDetails
//import com.example.denso.dispatch.model.RfidTag
//
//@Dao
//interface LocalDataBaseDao {
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun addDispatchResponsesInDb(binDispatchDetails: RfidTag)
//
//    @Query("SELECT * FROM RfidNumberTable")
//    suspend fun getAllData(): List<RfidTag>
//}