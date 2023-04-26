package com.example.denso.bin_scrap.model


import com.google.gson.annotations.SerializedName

data class BinScrapModel(
    @SerializedName("rfidNo")
    val rfidNo: String,

)