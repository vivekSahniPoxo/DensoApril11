package com.example.denso.user_action.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.android.dev.poxo.useraction.model.UserLoginModel

import com.example.denso.user_action.model.PlantName
import com.example.denso.user_action.repo.UserActionRepository
import com.example.denso.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.IdentityHashMap
import javax.inject.Inject

@HiltViewModel
class UserActionViewModel @Inject constructor(private val userActionRepository: UserActionRepository):ViewModel() {

    // this responseLiveData used to displayed the plants name
    val getPlantNameResponseLiveData:LiveData<NetworkResult<PlantName>>get() = userActionRepository.getPlantNameResponseLiveData

    val userResponseLiveData:LiveData<NetworkResult<UserLoginModel>>get() = userActionRepository.userResponseLiveData


    // this methoud used to displayed the plants name
    fun getPlantName(){
        viewModelScope.launch {
            userActionRepository.getPlantName()
        }
    }

    fun loginCredentials(userName:String,password:String,plantId:String){
        viewModelScope.launch {
            userActionRepository.loginCredentials(userName,password,plantId)
        }
    }
}