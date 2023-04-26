package com.example.denso.bin_scrap.model


import com.google.gson.annotations.SerializedName

data class ScrapResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("traceId")
    val traceId: String,
    @SerializedName("type")
    val type: String
)