package com.example.denso.bin_repair.model


import com.google.gson.annotations.SerializedName

data class BinRepairModel(
    @SerializedName("rfidNumber")
    val rfidNumber: String,
    @SerializedName("status")
    var status: String
)