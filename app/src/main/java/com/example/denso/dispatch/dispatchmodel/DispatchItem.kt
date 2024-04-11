package com.example.denso.dispatch.dispatchmodel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DispatchedItem(
    @SerializedName("rfidNumber")   val rfidTagNo: String,
    @SerializedName("status") var status: String,
    // @SerializedName("status") val status2: String,
    @SerializedName("partNo") val partName:String,
    @SerializedName("pkgGroupName")val pkgGroupName:String,
    @SerializedName("createdBy") val createdBy:String,
    @SerializedName("customer") val customer:String,
    @SerializedName("silNo") val silNo:String,
    @SerializedName("found") var found:String,
    @SerializedName("notFound") var notFound:String) : Parcelable


data class Item(val rfidTagNo: String, val status: String, val partName: String, val pkgGroupName: String)

data class GroupedItem(val groupName: String, val items: List<Item>)
