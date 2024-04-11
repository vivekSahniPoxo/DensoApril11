package com.example.denso.dispatch.dispatchmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denso.bin_recieving.BinReceivingDataModel
import com.example.denso.dispatch.model.*
import com.example.denso.utils.NetworkResult
import com.example.denso.dispatch.repo.DispatchRepository
import com.example.denso.utils.Event
import com.example.denso.utils.ToastEvent
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

    fun confirmReceiving(createRfidStatus: ArrayList<BinReceivingDataModel>){
        viewModelScope.launch {
            dispatchRepository.confirmReceiving(createRfidStatus).collect{
                _createDispatch.postValue(it)
            }
        }
    }



    private var _confirmDispatchLiveData = MutableLiveData<NetworkResult<String>>()
    val confirmDispatchLiveData:LiveData<NetworkResult<String>> = _confirmDispatchLiveData

    fun confirmDispatch(createRfidStatus: ArrayList<TempDispatch.TempItem>){
        viewModelScope.launch {
            dispatchRepository.confirmDispatch(createRfidStatus).collect{
                _confirmDispatchLiveData.postValue(it)
            }
        }
    }


    private var _toastLiveData = MutableLiveData<Event<String>>()
    val toastLiveData: LiveData<Event<String>> = _toastLiveData

    fun showToast(message: String) {
        _toastLiveData.value = ToastEvent(message)
    }




    private var _getDispatchItemLiveData = MutableLiveData<NetworkResult<BinDispatchDetails>>()
    val getDispatchItemLiveData: LiveData<NetworkResult<BinDispatchDetails>> = _getDispatchItemLiveData

    fun getDispatchItem(SilNo: String) {
        viewModelScope.launch {
            dispatchRepository.getDispatchItem(SilNo).collect {
                _getDispatchItemLiveData.postValue(it)
            }
        }
    }




}