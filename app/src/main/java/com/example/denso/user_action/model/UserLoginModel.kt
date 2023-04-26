package com.android.dev.poxo.useraction.model

import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.google.gson.annotations.SerializedName

@Keep
data class UserLoginModel (
    @SerializedName("username")val username:String,
    @SerializedName("password") val password:String,
    @SerializedName("plantid")val plantid:String)