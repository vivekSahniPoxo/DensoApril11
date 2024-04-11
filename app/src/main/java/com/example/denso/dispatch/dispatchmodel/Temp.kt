package com.example.denso.dispatch.dispatchmodel



import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize



@Parcelize
class TempDispatch : ArrayList<TempDispatch.TempItem>(), Parcelable {
    data class TempItem(
        @SerializedName("ctn")
        val ctn: Int,
        @SerializedName("createdby")
        val createdby: String,
        @SerializedName("customer")
        val customer: String,
        @SerializedName("found")
        var found: Int,
        @SerializedName("notFound")
        var notFound: Int,
        @SerializedName("partNo")
        val partNo: String,
        @SerializedName("pkgGroupName")
        val pkgGroupName: String,
        @SerializedName("rfidNumber")
        var rfidNumber: List<RfidNumber>,
        @SerializedName("silNo")
        val silNo: String,

    ) {
        data class RfidNumber(
            @SerializedName("rfidNumber")
            val rfidNumber: String,
            @SerializedName("status")
            var status: String,




        )
    }





}

data class GroupInfo(val pkgGroupName: String, val rfidNumber: String)