package com.example.denso.dispatch.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.denso.utils.NetworkResult
import com.example.denso.api.Apies
import com.example.denso.bin_recieving.BinReceivingDataModel
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.dispatch.dispatchmodel.TempDispatch
import com.example.denso.dispatch.model.BinDispatchDetails
import com.example.denso.dispatch.model.CreateRfidStatus
import com.example.denso.dispatch.model.DispatchModel
import com.example.denso.dispatch.model.RfidTag
import com.google.gson.JsonArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONException
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


    suspend fun confirmReceiving(createRfidStatus: ArrayList<BinReceivingDataModel>) = flow{
        emit(NetworkResult.Loading())
        val response = apies.rfidTagStatusFlow(createRfidStatus)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }

    suspend fun confirmDispatch(createRfidStatus: ArrayList<TempDispatch.TempItem>) = flow{
        emit(NetworkResult.Loading())
        val response = apies.rfidTagStatusForConfirmDispatch(createRfidStatus)
        emit(NetworkResult.Success(response))
    }.catch { e->
        emit(NetworkResult.Error(e.message ?: "UnknownError"))
    }


//    suspend fun getDispatchItem(SilNo: String) = flow {
//        emit(NetworkResult.Loading())
//        val response = apies.getDispatch(SilNo)
//        emit(NetworkResult.Success(response))
//    }.catch { e ->
//        emit(NetworkResult.Error(e.message ?: "UnknownError"))
//    }


    suspend fun getDispatchItem(SilNo: String) = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apies.getDispatch(SilNo)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "UnknownError"))
        }
    }.flowOn(Dispatchers.IO) // Specify IO dispatcher for network operations




//    private fun handleUserCredentialsResponse(response: Response<BinDispatchDetails>){
//        try {
//            if (response.isSuccessful && response.body() !=null){
//                _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
//            }
//                else if (response.errorBody()!=null){
//                    val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
//                _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("")))
//                }
//            else {
//                _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
//            }
//
//        }
//        catch (e:Exception){
//
//        }
//    }






    private fun handleUserCredentialsResponse(response: Response<BinDispatchDetails>) {
        try {
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody is BinDispatchDetails) {
                        _userResponseLiveData.postValue(NetworkResult.Success(responseBody))
                    } else {
                        _userResponseLiveData.postValue(NetworkResult.Error("Unexpected response format"))
                    }
                } else {
                    _userResponseLiveData.postValue(NetworkResult.Error("Response body is null"))
                }
            } else if (response.errorBody() != null) {
                val errorBody = response.errorBody()!!.string()
                Log.e("API Error", errorBody)

                // Check if the error body contains a specific string indicating no related data
                if (errorBody.contains("There is no Related Data For This Silnumber")) {
                    _userResponseLiveData.postValue(NetworkResult.Error("There is no related data to this SilNo"))
                } else {
                    _userResponseLiveData.postValue(NetworkResult.Error("Error in response: $errorBody"))
                }
            } else {
                _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            }
        } catch (e: Exception) {
            Log.e("API Error", "Error processing response: ${e.message}")
            _userResponseLiveData.postValue(NetworkResult.Error("Error processing response"))
        }
    }















//    private fun handleUserCredentialsResponse(response: Response<BinDispatchDetails>) {
//        try {
//            if (response.isSuccessful) {
//                if (response.body() != null) {
//                    _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
//                } else {
//                    _userResponseLiveData.postValue(NetworkResult.Error("Response body is null"))
//                }
//            } else if (response.errorBody() != null) {
//                val errorBody = response.errorBody()!!.string()
//                val contentType = response.errorBody()!!.contentType()
//
//                if (contentType?.subtype == "json") {
//                    val errorObj = JSONObject(errorBody)
//                    _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("")))
//                } else {
//                    // Handle non-JSON error body
//                    _userResponseLiveData.postValue(NetworkResult.Error("Non-JSON error body: $errorBody"))
//                }
//            } else {
//                _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
//            }
//
//        } catch (e: Exception) {
//            // Handle any exceptions that might occur during parsing or processing
//            _userResponseLiveData.postValue(NetworkResult.Error("Error processing response: ${e.message}"))
//        }
//    }



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