//package com.example.denso.local_database.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//
//import com.example.denso.dispatch.model.RfidTag
//import com.example.denso.local_database.LocaleDataBase
//import com.example.denso.local_database.repositiory.LocaleDataBaseRepository
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class LocalDataBaseViewModel(application: Application): AndroidViewModel(application) {
//
//
//    private var repository: LocaleDataBaseRepository
//
//    init {
//        val userDao = LocaleDataBase.getDatabase(application).localDataBaseDao()
//        repository = LocaleDataBaseRepository(userDao)
//    }
//
//    fun addAllRfidTag(rfidTag: RfidTag){
//        viewModelScope.launch(Dispatchers.Main) {
//            repository.addDispatchResponsesInDb(rfidTag)
//        }
//    }
//
//    private val _allData = MutableLiveData<List<RfidTag>>()
//    val allData: LiveData<List<RfidTag>> get() = _allData
//
//    fun fetchData() {
//        viewModelScope.launch {
//            _allData.value = repository.getAllData()
//        }
//    }
//}