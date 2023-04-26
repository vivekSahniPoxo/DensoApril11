package com.example.denso.bin_repair.model

import com.google.gson.annotations.SerializedName

data class OutRepairModelItem(
    @SerializedName("rfidNumber")
    val rfidNumber: ArrayList<String>,
    @SerializedName("status")
    val status: String
)