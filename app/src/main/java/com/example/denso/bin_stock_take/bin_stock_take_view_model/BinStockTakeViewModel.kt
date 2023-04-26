package com.example.denso.bin_stock_take.bin_stock_take_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

}