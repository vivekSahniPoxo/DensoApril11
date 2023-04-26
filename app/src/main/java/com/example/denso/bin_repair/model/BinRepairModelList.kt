package com.example.denso.bin_repair.model


import com.google.gson.annotations.SerializedName

class BinRepairModelList : ArrayList<BinRepairModelList.BinRepairModelListItem>(){
    data class BinRepairModelListItem(
        @SerializedName("rfidNumber")
        val rfidNumber: String,
        @SerializedName("status")
        val status: String
    )
}