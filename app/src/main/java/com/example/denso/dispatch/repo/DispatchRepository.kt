package com.example.denso.dispatch.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.denso.utils.NetworkResult
import com.example.denso.api.Apies
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.dispatch.model.BinDispatchDetails
import com.example.denso.dispatch.model.CreateRfidStatus
import com.example.denso.dispatch.model.DispatchModel
import com.example.denso.dispatch.model.RfidTag
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class DispatchRepository @Inject constructor(private val apies: Apies) {

    private val _userResponseLiveData = MutableLiveData<NetworkResult<BinDispatchDetails>>()
    val userResponseLiveData: LiveData<NetworkResult<BinDispatchDetails>>
        get() = _userResponseLiveData


    private val _dispatchStatusCreateLiveData = MutableLiveData<NetworkResult<CreateRfidStatus>>()
    val dispatchStatusCreateLiveData:LiveData<NetworkResult<CreateRfidStatus>>get() = _dispatchStatusCreateLiveData


    suspend fun dispatch(silInfo: String){
        _userResponseLiveData.postValue(NetworkResult.Loading())
        val response = apies.dispatch(silInfo)
        handleUserCredentialsResponse(response)
    }

    suspend fun createDispatchStatus(createRfidStatus: CreateRfidStatus){
        _dispatchStatusCreateLiveData.postValue(NetworkResult.Loading())
        val response = apies.rfidTagStatus(createRfidStatus)
        handleCreateRfidTagStatus(response)

    }


    suspend fun confirmReceiving(createRfidStatus: ArrayList<BinRepairModel>) = flow{
        emit(NetworkResult.Loading())
        val response = apies.rfidTagStatusFlow(createRfidStatus)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }

    suspend fun confirmDispatch(createRfidStatus: ArrayList<RfidTag>) = flow{
        emit(NetworkResult.Loading())
        val response = apies.rfidTagStatusForConfirmDispatch(createRfidStatus)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }



    private fun handleUserCredentialsResponse(response: Response<BinDispatchDetails>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
                else if (response.errorBody()!=null){
                    val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("title")))
                }
            else {
                _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            }

        }
        catch (e:Exception){


        }
    }


    private fun handleCreateRfidTagStatus(response: Response<CreateRfidStatus>){
        try {
            if (response.isSuccessful && response.body() !=null){
                _dispatchStatusCreateLiveData.postValue(NetworkResult.Success(response.body()!!))
            }
            else if (response.errorBody()!=null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _dispatchStatusCreateLiveData.postValue(NetworkResult.Error(errorObj.getString("title")))
            }
            else {
                _dispatchStatusCreateLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            } }
        catch (e:Exception){


        }
    }


}