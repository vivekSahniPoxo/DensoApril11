package com.example.denso.user_action.repo

import androidx.lifecycle.MutableLiveData
import com.android.dev.poxo.useraction.model.UserLoginModel

import com.example.denso.api.Apies
import com.example.denso.user_action.model.PlantName
import com.example.denso.utils.NetworkResult
import org.json.JSONObject
import javax.inject.Inject

class UserActionRepository @Inject constructor(private val allApies: Apies) {

    private val _getPlantNameResponseLiveData = MutableLiveData<NetworkResult<PlantName>>()
    val getPlantNameResponseLiveData get() = _getPlantNameResponseLiveData

    private val _userResponseLiveData = MutableLiveData<NetworkResult<UserLoginModel>>()
    val userResponseLiveData get() = _userResponseLiveData



    suspend fun getPlantName(){
        try{
            _getPlantNameResponseLiveData.postValue(NetworkResult.Loading())
              val response  = allApies.getPlantName()
            if (response.isSuccessful && response.body() !=null){
                _getPlantNameResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            } else if (response.errorBody() != null){
                _getPlantNameResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            }


        }catch (e:Exception){

        }
    }

    suspend fun loginCredentials(username:String,password:String,plantId:String){
        try {
           _userResponseLiveData.postValue(NetworkResult.Loading())
           val response = allApies.loginCredentials(username,password,plantId)
            if (response.isSuccessful && response.body() != null){
                _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            } else if (response.errorBody() != null){
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("UserId or Password wrong")))
            } else {
                _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
            }

        } catch (e:Exception){

        }
    }




}