package com.example.denso.bin_repair.model

import com.google.gson.annotations.SerializedName

data class BinRepairModelListItem(@SerializedName("rfidNumber") val rfidNumber: String, @SerializedName("status") val status: String)