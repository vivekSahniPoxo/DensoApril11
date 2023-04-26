package com.example.denso.bin_stock_take.bin_stock_take_repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.denso.utils.NetworkResult
import com.example.denso.api.Apies
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class BinStockTakeRepository @Inject constructor(private val apies: Apies) {
    private val _binStockTakeResponseLiveData = MutableLiveData<NetworkResult<BinStockResponseFromApiModel>>()
    val binStockTakeResponseLiveData: LiveData<NetworkResult<BinStockResponseFromApiModel>> get() = _binStockTakeResponseLiveData


    suspend fun binStockTakeRepository(binStockTakeModel: ArrayList<String>){
        _binStockTakeResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.binStockTake(binStockTakeModel)
        handlerBinStockResponse(response)

    }


    private fun handlerBinStockResponse(response: Response<BinStockResponseFromApiModel>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _binStockTakeResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _binStockTakeResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("string")))
            }
            else {
                _binStockTakeResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            } }
        catch (e:Exception){


        }
    }
}