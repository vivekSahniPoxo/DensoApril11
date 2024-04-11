package com.example.denso.dispatch.model


import com.google.gson.annotations.SerializedName

class BinDispatchRecieveModel : ArrayList<BinDispatchRecieveModel.BinDispatchRecieveModelItem>(){
    data class BinDispatchRecieveModelItem(
        @SerializedName("createdby")
        val createdby: String,
        @SerializedName("customer")
        val customer: String,
        @SerializedName("found")
        val found: Int,
        @SerializedName("notFound")
        val notFound: Int,
        @SerializedName("partNo")
        val partNo: String,
        @SerializedName("pkgGroupName")
        val pkgGroupName: String,
        @SerializedName("rfidNumber")
        val rfidNumber: List<RfidNumber>,
        @SerializedName("silNo")
        val silNo: String
    ) {
        data class RfidNumber(
            @SerializedName("rfidNumber")
            val rfidNumber: String,
            @SerializedName("status")
            val status: String
        )
    }
}