package com.example.denso.dispatch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

class BinDispatchDetails : ArrayList<BinDispatchDetails.BinDispatchDetailsItem>(){
    @Keep
    data class BinDispatchDetailsItem(
//      @SerializedName("ctn")  val ctn: Int,
//      @SerializedName("customerName")  val customerName: String,
//      @SerializedName("groupName") val groupName: String,
//      @SerializedName("lotSize")val lotSize: Int,
//      @SerializedName("partName")  val partName: String,
//      @SerializedName("partNo")  val partNo: String,
//     // @SerializedName("pkgPartNo")  val pkgPartNo: String,
//      @SerializedName("rfidNumber")  val rfidNumber: List<RfidNumber>,
//      @SerializedName("shipQty")  val shipQty: String,
//      @SerializedName("weight")  val weight: String
//    ) {
//        data class RfidNumber(
//        @SerializedName("rfidTagNo")   val rfidTagNo: String,
//         @SerializedName("status")   val status: Int
//        )
//    }
        @SerializedName("ctn")
        val ctn: Int,
        @SerializedName("customerName")
        val customerName: String,
        @SerializedName("groupName")
        val groupName: String,
        @SerializedName("lotSize")
        val lotSize: Int,
        @SerializedName("partName")
        val partName: String,
        @SerializedName("partNo")
        val partNo: String,
        @SerializedName("pkgPartNo")
        val pkgPartNo: Any,
        @SerializedName("rfidNumber")
        val rfidNumber: List<RfidNumber>,
        @SerializedName("shipQty")
        val shipQty: String,
        @SerializedName("weight")
        val weight: String
    ) {
        data class RfidNumber(
            @SerializedName("groupId")
            val groupId: Int,
            @SerializedName("rfidTagHex")
            val rfidTagHex: Any,
            @SerializedName("rfidTagNo")
            val rfidTagNo: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("statusCode")
            val statusCode: Int
        )
    }
}