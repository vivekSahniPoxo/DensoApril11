package com.example.denso.dispatch.model

import com.google.gson.annotations.SerializedName


data class DispatchItem(
         @SerializedName("partNo")val PartNo: String,
        @SerializedName("quatinty")val Quatinty: String,
         @SerializedName("parData") val partData:String
    )
