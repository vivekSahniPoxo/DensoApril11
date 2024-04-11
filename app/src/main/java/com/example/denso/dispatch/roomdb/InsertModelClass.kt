package com.example.denso.dispatch.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RFID_Table")
data class InsertModelClass (
    @PrimaryKey(autoGenerate = true)
    val Id:Int,
    var rfidNo:String,
    var status:String)