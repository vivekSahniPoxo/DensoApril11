package com.example.denso.bin_repair.bin_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denso.bin_repair.model.*
import com.example.denso.utils.NetworkResult
import com.example.denso.bin_repair.repositry.BinRepairRepository
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_scrap.model.ScrapResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinRepairViewModel @Inject constructor(private val binRepairRepository: BinRepairRepository):ViewModel() {


    val binRepairResponseLiveData: LiveData<NetworkResult<BinRepairModel>>
    get() = binRepairRepository.binRepairResponseLiveData

    val binOutRepairResponseLiveData: LiveData<NetworkResult<OutResponseFromApi>>
        get() = binRepairRepository.binOutRepairResponseLiveData


    private var _getResponseFromBinRepairIn = MutableLiveData<NetworkResult<String>>()
    val getResponseFromBinRepairIn:LiveData<NetworkResult<String>> = _getResponseFromBinRepairIn


    private var _getResponseFromBinRepairOut = MutableLiveData<NetworkResult<String>>()
    val getResponseFromBinRepairOt:LiveData<NetworkResult<String>> = _getResponseFromBinRepairOut



    fun binRepairStatus(binRepairModel: ArrayList<String>) {
        viewModelScope.launch {
            binRepairRepository.binRepairStatus(binRepairModel)
        }
    }

    fun binOutRepair(outRepairModel: ArrayList<BinRepairModel>){
        viewModelScope.launch {
            binRepairRepository.binOutRepairModel(outRepairModel)
        }
    }


    fun binRepairIn(binRepairModel:  ArrayList<BinRepairModel>){
        viewModelScope.launch {
            binRepairRepository.binRepairIn(binRepairModel).collect{
                _getResponseFromBinRepairIn.postValue(it)
            }
        }
    }




    fun binRepairOutFlow(outRepairModel: ArrayList<BinRepairModel>){
        viewModelScope.launch {
            binRepairRepository.binRepairOut(outRepairModel).collect{
                _getResponseFromBinRepairIn.postValue(it)
            }
        }
    }


}