package com.example.denso.bin_stock_take.model


import com.google.gson.annotations.SerializedName

class BinStockResponseFromApiModel : ArrayList<BinStockResponseFromApiModel.BinStockResponseFromApiModelItem>(){
    data class BinStockResponseFromApiModelItem(
        @SerializedName("binQty")
        val binQty: Int,
        @SerializedName("groupName")
        val groupName: String,
        @SerializedName("partNo")
        val partNo: String,
        @SerializedName("pkgLotSize")
        val pkgLotSize: Int,
        @SerializedName("pkgPartNo")
        val pkgPartNo: String,
        @SerializedName("rfidNumber")
        val rfidNumber: String,
        @SerializedName("status")
        val status: Int,
        @SerializedName("binId")
        val binId:String
    )
}