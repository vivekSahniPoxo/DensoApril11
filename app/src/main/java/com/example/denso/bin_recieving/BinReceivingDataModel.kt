package com.example.denso.bin_recieving

import com.google.gson.annotations.SerializedName



data class BinReceivingDataModel(
    @SerializedName("rfidTagNo")
    val rfidNumber: String,
    @SerializedName("status")
    var status: String,
    @SerializedName("createdby")
    val createdby: String
)