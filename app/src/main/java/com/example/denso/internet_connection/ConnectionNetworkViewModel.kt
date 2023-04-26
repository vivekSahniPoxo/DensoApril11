package com.example.denso.internet_connection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ConnectionNetworkViewModel @Inject constructor(listenNetwork: ListenNetwork): ViewModel() {
    val isConnected: Flow<Boolean> = listenNetwork.isConnected
}