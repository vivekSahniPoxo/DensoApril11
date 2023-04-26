package com.example.denso.bin_scrap.bin_scrap_view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denso.utils.NetworkResult
import com.example.denso.bin_scrap.bin_scrap_repository.BinScrapRepository
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_scrap.model.ScrapResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class BinScrapViewModel @Inject constructor(private val scrapRepository: BinScrapRepository):ViewModel() {


    val binScrapViewModelResponseLiveData: LiveData<NetworkResult<String>>
        get() = scrapRepository.binScrapResponseLiveData

    private var _getResponseOfScrap = MutableLiveData<NetworkResult<String>>()
    val getResponseOfScrap:LiveData<NetworkResult<String>> = _getResponseOfScrap

    fun binScrap(binScrapModel: BinScrapModel) {
        viewModelScope.launch {
            scrapRepository.binScrap(binScrapModel)
        }
    }


    fun binScrapFlow(binScrapModel: BinScrapModel){
        viewModelScope.launch {
            scrapRepository.binScrapFlow(binScrapModel).collect{
                _getResponseOfScrap.postValue(it)
            }
        }
    }






}