package com.example.denso.dispatch.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//@Parcelize
//data class RfidTag(
//    @SerializedName("rfidNumber")   val rfidTagNo: String,
//    @SerializedName("status") var status: String,) : Parcelable

@Parcelize
data class RfidTag(
    @SerializedName("rfidNumber")   val rfidTagNo: String,
    @SerializedName("status") var status: String,
   // @SerializedName("status") val status2: String,
    @SerializedName("partNo") val partName:String,
    @SerializedName("pkgGroupName")val pkgGroupName:String,
    @SerializedName("customer") val customer:String) : Parcelable