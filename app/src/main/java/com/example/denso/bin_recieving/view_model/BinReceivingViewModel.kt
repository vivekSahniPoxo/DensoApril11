package com.example.denso.bin_recieving.view_model

import androidx.lifecycle.ViewModel
import com.example.denso.bin_recieving.repository.BinReceivingRepository
import javax.inject.Inject

class BinReceivingViewModel @Inject constructor(private val binReceivingRepository: BinReceivingRepository):ViewModel() {
}