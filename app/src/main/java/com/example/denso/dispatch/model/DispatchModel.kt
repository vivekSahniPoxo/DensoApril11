package com.example.denso.dispatch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DispatchModel(
    @SerializedName("CustomerID") val customerID: String,
    @SerializedName("dispatchItems") val dispatchItems: ArrayList<DispatchItem>,
    @SerializedName("TruckNo") val truckNo: String)

