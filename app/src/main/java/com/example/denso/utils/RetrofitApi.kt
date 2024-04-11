package com.example.denso.utils



import com.example.denso.dispatch.dispatchmodel.TempDispatch
import com.example.denso.dispatch.model.BinDispatchDetails
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitApi {

    @POST("/api/bin-dispatch")
    fun updateVehicleInfo(@Body registrationDataModel:ArrayList<TempDispatch.TempItem>):Call<String>


    @GET("/api/bin-details-groupwise")
     fun getDispatchMVC(@Query("Silinfo") silinfo: String): Call<BinDispatchDetails>

}