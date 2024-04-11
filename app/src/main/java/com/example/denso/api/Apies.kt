package com.example.denso.api

import com.android.dev.poxo.useraction.model.UserLoginModel
import com.example.denso.bin_recieving.BinReceivingDataModel
import com.example.denso.bin_repair.model.*
import com.example.denso.bin_scrap.BinScrap
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_scrap.model.ScrapResponse
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import com.example.denso.bin_stock_take.model.BinStockTakeModel
import com.example.denso.dispatch.dispatchmodel.TempDispatch
import com.example.denso.dispatch.model.*
import com.example.denso.user_action.model.PlantName
import okhttp3.RequestBody
import org.json.JSONObject

import retrofit2.Response
import retrofit2.http.*

interface Apies {
    @GET("/api/get-plantlocation")
    suspend fun getPlantName():Response<PlantName>

    @GET("/api/validate-user")
    suspend fun loginCredentials(@Query("username")userName:String, @Query("password")password:String,
                                 @Query("plantid")plantid:String):Response<UserLoginModel>

     //@GET("/api/bin-details")
    @GET("/api/bin-details-groupwise")
    suspend fun dispatch(@Query ("Silinfo") silinfo:String):Response<BinDispatchDetails>

    @GET("/api/bin-details-groupwise")
    suspend fun getDispatch(@Query("Silinfo") silinfo: String): BinDispatchDetails


    @POST("/api/dispatch-status-create")
    suspend fun rfidTagStatus(@Body createRfidStatus: CreateRfidStatus):Response<CreateRfidStatus>



    //@POST("dispatch-status-create")
    //@POST("/api/bin-dispatch/receive")
    @POST("/api/bin-receive")
    suspend fun rfidTagStatusFlow(@Body createRfidStatus: ArrayList<BinReceivingDataModel>):String

//    @POST("dispatch-status-create")
    //@POST("/api/bin-dispatch/receive")
    //@POST("/api/bin-receive")
    @POST("/api/bin-dispatch")
    suspend fun rfidTagStatusForConfirmDispatch(@Body createRfidStatus: ArrayList<TempDispatch.TempItem>):String

    @POST("/api/repair-bin/")
    suspend fun binRepair(@Body binRepairModel: ArrayList<String>):Response<BinRepairModel>

    @POST("/api/repair-bin")
    suspend fun binRepairFlow(@Body binRepairModel:  ArrayList<BinRepairModel>):String


    @POST("/api/repair-bin-out")
    suspend fun outFromBinRepairOutflow(@Body outRepairModel: ArrayList<BinRepairModel>):String

    @POST("/api/repair-bin-out")
    suspend fun outFromBinRepair(@Body outRepairModel: ArrayList<BinRepairModel>):Response<OutResponseFromApi>

    @POST("/api/stock-take-screen")
    suspend fun binStockTake(@Body binStockTakeModel: ArrayList<String>):Response<BinStockResponseFromApiModel>

    @POST("/api/shankyu-receive")
    suspend fun binShankyuReceive(@Body shankyuReceive: ArrayList<String>):String

    @POST("/api/shankyu-dispatch")
    suspend fun shankyuDispatch(@Body shankyuReceive: ArrayList<String>):String

    @POST("/api/Scrap-tags-device")
    suspend fun binScrap(@Body  binScrapModel: BinScrapModel):Response<String>

    @POST("/api/Scrap-tags-device")
    suspend fun binScrapFlow(@Body binScrapModel: BinScrapModel):String


}