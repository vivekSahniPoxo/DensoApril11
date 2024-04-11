package com.example.denso.bin_stock_take.bin_stock_take_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denso.bin_recieving.BinReceivingDataModel
import com.example.denso.utils.NetworkResult
import com.example.denso.bin_stock_take.bin_stock_take_repository.BinStockTakeRepository
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinStockTakeViewModel @Inject constructor(private val binStockTakeRepository: BinStockTakeRepository):ViewModel() {


    val binStockTakeModelResponseLiveData: LiveData<NetworkResult<BinStockResponseFromApiModel>>
        get() = binStockTakeRepository.binStockTakeResponseLiveData


    fun binStockTake(binStockTakeModel: ArrayList<String>) {
        viewModelScope.launch {
            binStockTakeRepository.binStockTakeRepository(binStockTakeModel)
        }
    }

    private var _createShankyuReceive = MutableLiveData<NetworkResult<String>>()
    val createShankyuReceive:LiveData<NetworkResult<String>> = _createShankyuReceive
    fun confirmReceiving(createRfidStatus: ArrayList<String>){
        viewModelScope.launch {
            binStockTakeRepository.shankyuReceive(createRfidStatus).collect{
                _createShankyuReceive.postValue(it)
            }
        }
    }


    private var _createShankyuDispatch = MutableLiveData<NetworkResult<String>>()
    val createShankyuDispatch:LiveData<NetworkResult<String>> = _createShankyuDispatch

    fun confirmDispatch(createRfidStatus: ArrayList<String>){
        viewModelScope.launch {
            binStockTakeRepository.shankyuDispatch(createRfidStatus).collect{
                _createShankyuDispatch.postValue(it)
            }
        }
    }


}