package com.example.denso.dispatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.denso.databinding.ActivityViewAllRfidTagsBinding
import com.example.denso.dispatch.adapter.AllScannedRfidStatusAdapter
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.utils.Cons

class ViewAllRfidTags : AppCompatActivity() {

    lateinit var binding:ActivityViewAllRfidTagsBinding
    lateinit var rfidList:ArrayList<RfidTag>
    lateinit var getAllScannedRfidStatusAdapter: AllScannedRfidStatusAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllRfidTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rfidList = arrayListOf()

        rfidList =  intent.getSerializableExtra(Cons.RFID) as ArrayList<RfidTag>

        if (rfidList.isEmpty()){
            binding.tvNoDataFound.isVisible= true
        } else {
            getAllScannedRfidStatusAdapter = AllScannedRfidStatusAdapter(rfidList)
            binding.rfidTagList.adapter = getAllScannedRfidStatusAdapter
            binding.tvNoDataFound.isVisible= false
        }


    }
}