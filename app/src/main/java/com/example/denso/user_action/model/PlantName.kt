package com.example.denso.user_action.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

@Keep
class PlantName : ArrayList<PlantName.PlantNameItem>(){
    data class PlantNameItem(
        @SerializedName("plantId") val plantId: Int,
        @SerializedName ("plantName")   val plantName: String
    )
}