package com.example.denso.dispatch.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//@Parcelize
//data class RfidTag(
//    @SerializedName("rfidNumber")   val rfidTagNo: String,
//    @SerializedName("status") var status: String,) : Parcelable

@Parcelize
//@Entity(tableName = "RfidNumberTable")
data class RfidTag(
    @SerializedName("rfidNumber")   val rfidTagNo: String,
    val partNo:String,
    @SerializedName("status") var status: String):Parcelable{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RfidTag) return false

        return rfidTagNo == other.rfidTagNo && status == other.status
    }

    override fun hashCode(): Int {
        var result = rfidTagNo.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
    }






//@Entity(tableName = "RfidNumberTable")
//data class RfidTag(
//    @PrimaryKey
//    val id:Int,
//    @SerializedName("rfidNumber")   val rfidTagNo: String,
//    @SerializedName("status") var status: String)

   // @SerializedName("status") val status2: String,
//    @SerializedName("partNo") val partName:String,
//    @SerializedName("pkgGroupName")val pkgGroupName:String,
//    @SerializedName("createdBy") val createdBy:String,
//    @SerializedName("customer") val customer:String,
//    @SerializedName("silNo") val silNo:String,
//    @SerializedName("found") var found:Int,
//    @SerializedName("notFound") var notFound:Int) : Parcelable


//@Entity(tableName = "RfidNumberTable")
//data class AddInDbRfidTag(
//
//    @SerializedName("rfidNumber")   val rfidTagNo: String,
//    @SerializedName("status") var status: String)
//



