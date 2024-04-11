package com.example.denso.dispatch.event_listener

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.denso.dispatch.model.BinDispatchDetails

interface RFIDECheckListener {
    fun onRfidListener(item:BinDispatchDetails.BinDispatchDetailsItem,root:ConstraintLayout)
}