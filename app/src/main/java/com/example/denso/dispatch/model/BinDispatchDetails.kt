package com.example.denso.dispatch.model

import com.google.gson.annotations.SerializedName


class BinDispatchDetails : ArrayList<BinDispatchDetails.BinDispatchDetailsItem>(){
    data class BinDispatchDetailsItem(
        @SerializedName("ctn")
        val ctn: Int,
        @SerializedName("customerAddress")
        val customerAddress: String,
        @SerializedName("customerCode")
         val customerCode: String,
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
        var rfidNumber: List<RfidNumber>,
        @SerializedName("shipQty")
        val shipQty: String,
        @SerializedName("weight")
        val weight: String,
        @SerializedName("silNo")
        val silNo: String,
        var isMatched: Boolean = false, // New property to track the matching condition
        var color:Int,
        var found:Int,
        var notFound:Int,
        var count:Int
    ) {
//        data class RfidNumber(
//            @SerializedName("binId")
//            val binId: String,
//            @SerializedName("groupId")
//            val groupId: Int,
//            @SerializedName("rfidTagHex")
//            val rfidTagHex: Any,
//            @SerializedName("rfidTagNo")
//            val rfidTagNo: String,
//            @SerializedName("status")
//            val status: String,
//            @SerializedName("statusCode")
//            val statusCode: Int
//        )


        data class RfidNumber(
            @SerializedName("binId")
            val binId: String?,
            @SerializedName("groupId")
            val groupId: Int?,
            @SerializedName("rfidTagHex")
            val rfidTagHex: Any?,
            @SerializedName("rfidTagNo")
            val rfidTagNo: String?,
            @SerializedName("status")
            var status: String,
            @SerializedName("statusCode")
            var statusCode: Int
        ) {
            override fun hashCode(): Int {
                return listOf(binId, groupId, rfidTagHex, rfidTagNo, status, statusCode).hashCode()
            }
        }

    }
//        @SerializedName("ctn")
//        val ctn: Int,
//        @SerializedName("customerAddress")
//        val customerAddress: String,
//        @SerializedName("customerName")
//        val customerName: String,
//        @SerializedName("groupName")
//        val groupName: String,
//        @SerializedName("lotSize")
//        val lotSize: Int,
//        @SerializedName("partName")
//        val partName: String,
//        @SerializedName("partNo")
//        val partNo: String,
//        @SerializedName("pkgPartNo")
//        val pkgPartNo: Any,
//        @SerializedName("rfidNumber")
//        val rfidNumber: List<RfidNumber>,
//        @SerializedName("shipQty")
//        val shipQty: String,
//        @SerializedName("weight")
//        val weight: String
//    ) {
//        data class RfidNumber(
//            @SerializedName("binId")
//            val binId: String,
//            @SerializedName("groupId")
//            val groupId: Int,
//            @SerializedName("rfidTagHex")
//            val rfidTagHex: Any,
//            @SerializedName("rfidTagNo")
//            val rfidTagNo: String,
//            @SerializedName("status")
//            val status: String,
//            @SerializedName("statusCode")
//            val statusCode: Int
//        )
//    }
}

//class BinDispatchDetails : ArrayList<BinDispatchDetails.BinDispatchDetailsItem>(){
//    @Keep
//    data class BinDispatchDetailsItem(
////      @SerializedName("ctn")  val ctn: Int,
////      @SerializedName("customerName")  val customerName: String,
////      @SerializedName("groupName") val groupName: String,
////      @SerializedName("lotSize")val lotSize: Int,
////      @SerializedName("partName")  val partName: String,
////      @SerializedName("partNo")  val partNo: String,
////     // @SerializedName("pkgPartNo")  val pkgPartNo: String,
////      @SerializedName("rfidNumber")  val rfidNumber: List<RfidNumber>,
////      @SerializedName("shipQty")  val shipQty: String,
////      @SerializedName("weight")  val weight: String
////    ) {
////        data class RfidNumber(
////        @SerializedName("rfidTagNo")   val rfidTagNo: String,
////         @SerializedName("status")   val status: Int
////        )
////    }
//        @SerializedName("ctn")
//        val ctn: Int,
//        @SerializedName("customerName")
//        val customerName: String,
//        @SerializedName("customerAddress")
//        val customerAddress:String,
//        @SerializedName("groupName")
//        val groupName: String,
//        @SerializedName("lotSize")
//        val lotSize: Int,
//        @SerializedName("partName")
//        val partName: String,
//        @SerializedName("partNo")
//        val partNo: String,
//        @SerializedName("pkgPartNo")
//        val pkgPartNo: Any,
//        @SerializedName("rfidNumber")
//        val rfidNumber: List<RfidNumber>,
//        @SerializedName("shipQty")
//        val shipQty: String,
//        @SerializedName("weight")
//        val weight: String
//    ) {
//        data class RfidNumber(
//            @SerializedName("groupId")
//            val groupId: Int,
//            @SerializedName("rfidTagHex")
//            val rfidTagHex: Any,
//            @SerializedName("rfidTagNo")
//            val rfidTagNo: String,
//            @SerializedName("status")
//            val status: String,
//            @SerializedName("statusCode")
//            val statusCode: Int
//        )
//    }
//}