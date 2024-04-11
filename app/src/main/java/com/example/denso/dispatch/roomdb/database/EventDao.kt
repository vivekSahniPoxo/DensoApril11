package com.example.denso.dispatch.roomdb.database

import androidx.room.*
import com.example.denso.dispatch.roomdb.InsertModelClass

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRFidS(userEntryDetails: InsertModelClass)

    @Query("SELECT * FROM RFID_Table")
    fun getAllData(): List<InsertModelClass>

    @Query("SELECT * FROM RFID_Table WHERE rfidNo = :rfidTag")
    fun getRfiNo(rfidTag: String): InsertModelClass

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInsertedData(rfidDetails: InsertModelClass)


}