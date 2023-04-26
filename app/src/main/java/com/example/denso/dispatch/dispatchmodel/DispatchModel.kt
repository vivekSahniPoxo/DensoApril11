package com.example.denso.dispatch.dispatchmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.dispatch.model.*
import com.example.denso.utils.NetworkResult
import com.example.denso.dispatch.repo.DispatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DispatchViewModel @Inject constructor(private val dispatchRepository: DispatchRepository): ViewModel() {

    val dispatchResponseLiveData: LiveData<NetworkResult<BinDispatchDetails>>
        get() = dispatchRepository.userResponseLiveData

    val dispatchCreateStatusLiveData: LiveData<NetworkResult<CreateRfidStatus>>
        get() = dispatchRepository.dispatchStatusCreateLiveData


    fun dispatch(silInfo: String) {
        viewModelScope.launch {
            dispatchRepository.dispatch(silInfo)
        }
    }


    fun dispatchCreateStatus(createRfidStatus: CreateRfidStatus) {
        viewModelScope.launch {
            dispatchRepository.createDispatchStatus(createRfidStatus)
        }
    }

    private var _createDispatch = MutableLiveData<NetworkResult<String>>()
    val createDispatch:LiveData<NetworkResult<String>> = _createDispatch

    fun confirmReceiving(createRfidStatus: ArrayList<BinRepairModel>){
        viewModelScope.launch {
            dispatchRepository.confirmReceiving(createRfidStatus).collect{
                _createDispatch.postValue(it)
            }
        }
    }



    private var _confirmDispatchLiveData = MutableLiveData<NetworkResult<String>>()
    val confirmDispatchLiveData:LiveData<NetworkResult<String>> = _confirmDispatchLiveData

    fun confirmDispatch(createRfidStatus: ArrayList<RfidTag>){
        viewModelScope.launch {
            dispatchRepository.confirmDispatch(createRfidStatus).collect{
                _confirmDispatchLiveData.postValue(it)
            }
        }
    }




}