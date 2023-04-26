package com.example.denso.bin_scrap.bin_scrap_repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.denso.utils.NetworkResult
import com.example.denso.api.Apies
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_scrap.model.ScrapResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class BinScrapRepository @Inject constructor(private val apies: Apies) {

    private val _binScrapResponseLiveData = MutableLiveData<NetworkResult<String>>()
    val binScrapResponseLiveData: LiveData<NetworkResult<String>> get() = _binScrapResponseLiveData

    suspend fun binScrap(binScrapModel: BinScrapModel){
        _binScrapResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.binScrap(binScrapModel)
        handleBinScrapResponse(response)

    }

    private fun handleBinScrapResponse(response: Response<String>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _binScrapResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _binScrapResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("title")))
            }
            else {
                _binScrapResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            } }
        catch (e:Exception){


        }
    }

    suspend fun binScrapFlow(binScrapModel: BinScrapModel) = flow{
        emit(NetworkResult.Loading())
        val response = apies.binScrapFlow(binScrapModel)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }



}