package com.example.denso.api

import com.android.dev.poxo.useraction.model.UserLoginModel
import com.example.denso.bin_repair.model.*
import com.example.denso.bin_scrap.BinScrap
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_scrap.model.ScrapResponse
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import com.example.denso.bin_stock_take.model.BinStockTakeModel
import com.example.denso.dispatch.model.*
import com.example.denso.user_action.model.PlantName
import okhttp3.RequestBody
import org.json.JSONObject

import retrofit2.Response
import retrofit2.http.*

interface Apies {
    @GET("get-plantlocation")
    suspend fun getPlantName():Response<PlantName>

    @GET("validate-user")
    suspend fun loginCredentials(@Query("username")userName:String, @Query("password")password:String,
                                 @Query("plantid")plantid:String):Response<UserLoginModel>

    @GET("bin-details")
    suspend fun dispatch(@Query ("Silinfo") silinfo:String):Response<BinDispatchDetails>

    @POST("dispatch-status-create")
    suspend fun rfidTagStatus(@Body createRfidStatus: CreateRfidStatus):Response<CreateRfidStatus>


    //@POST("dispatch-status-create")
    @POST("bin-dispatch/receive")
    suspend fun rfidTagStatusFlow(@Body createRfidStatus: ArrayList<BinRepairModel>):String

//    @POST("dispatch-status-create")
    @POST("bin-dispatch/receive")
    suspend fun rfidTagStatusForConfirmDispatch(@Body createRfidStatus: ArrayList<RfidTag>):String

    @POST("repair-bin/")
    suspend fun binRepair(@Body binRepairModel: ArrayList<String>):Response<BinRepairModel>

    @POST("repair-bin")
    suspend fun binRepairFlow(@Body binRepairModel:  ArrayList<BinRepairModel>):String

    @POST("repair-bin-out")
    suspend fun outFromBinRepair(@Body outRepairModel: ArrayList<BinRepairModel>):Response<OutResponseFromApi>

    @POST("stock-take-screen")
    suspend fun binStockTake(@Body binStockTakeModel: ArrayList<String>):Response<BinStockResponseFromApiModel>

    @POST("Scrap-tags-device")
    suspend fun binScrap(@Body  binScrapModel: BinScrapModel):Response<String>

    @POST("Scrap-tags-device")
    suspend fun binScrapFlow(@Body binScrapModel: BinScrapModel):String


}