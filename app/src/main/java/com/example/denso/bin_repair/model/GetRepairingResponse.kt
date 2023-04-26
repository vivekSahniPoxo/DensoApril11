package com.example.denso.bin_repair.model


import com.google.gson.annotations.SerializedName

data class GetRepairingResponse(
    @SerializedName("string")
    val string: String
)