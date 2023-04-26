package com.example.denso.bin_repair.repositry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.denso.utils.NetworkResult
import com.example.denso.api.Apies
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.bin_repair.model.BinRepairModelList
import com.example.denso.bin_repair.model.BinRepairModelListItem
import com.example.denso.bin_repair.model.OutResponseFromApi
import com.example.denso.bin_scrap.model.BinScrapModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class BinRepairRepository @Inject constructor(private val apies: Apies) {
    private val _binRepairResponseLiveData = MutableLiveData<NetworkResult<BinRepairModel>>()
    val binRepairResponseLiveData: LiveData<NetworkResult<BinRepairModel>> get() = _binRepairResponseLiveData

    private val _binOutRepairResponseLiveData = MutableLiveData<NetworkResult<OutResponseFromApi>>()
    val binOutRepairResponseLiveData: LiveData<NetworkResult<OutResponseFromApi>> get() = _binOutRepairResponseLiveData

    suspend fun binRepairStatus(binRepairModel: ArrayList<String>){
        _binRepairResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.binRepair(binRepairModel)
        handlerBinRepairResponse(response) }


    suspend fun binRepairIn(binRepairModel:  ArrayList<BinRepairModel>) = flow{
        emit(NetworkResult.Loading())
        val response = apies.binRepairFlow(binRepairModel)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }




    suspend fun binOutRepairModel(outRepairModel: ArrayList<BinRepairModel>){
        _binRepairResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.outFromBinRepair(outRepairModel)
        handleBinOutRepairResponse(response)

    }

    private fun handlerBinRepairResponse(response: Response<BinRepairModel>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _binRepairResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _binRepairResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("title")))
            }
            else {
                _binRepairResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            } }
        catch (e:Exception){


        }
    }

    private fun handleBinOutRepairResponse(response: Response<OutResponseFromApi>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _binOutRepairResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _binOutRepairResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
            }
            else {
                _binOutRepairResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            } }
        catch (e:Exception){


        }
    }

}