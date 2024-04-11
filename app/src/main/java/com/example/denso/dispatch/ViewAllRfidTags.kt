package com.example.denso.dispatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.denso.databinding.ActivityViewAllRfidTagsBinding
import com.example.denso.dispatch.adapter.AllScannedRfidStatusAdapter
import com.example.denso.dispatch.adapter.ItemDattaAdpter
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.dispatch.roomdb.InsertModelClass
import com.example.denso.dispatch.roomdb.database.EventDao
import com.example.denso.dispatch.roomdb.database.EventDatabase
import com.example.denso.utils.Cons
import com.example.denso.utils.DataStorage

class ViewAllRfidTags : AppCompatActivity() {

    lateinit var binding:ActivityViewAllRfidTagsBinding
    lateinit var rfidList:ArrayList<RfidTag>
    lateinit var getAllScannedRfidStatusAdapter: AllScannedRfidStatusAdapter
    lateinit var itemDattaAdpter: ItemDattaAdpter
    val isCommingFrom = false
  //  lateinit var myEventDataBase: EventDatabase
//    lateinit var eventDao: EventDao
    lateinit var mList:ArrayList<InsertModelClass>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllRfidTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        try {
//            myEventDataBase = EventDatabase.getDatabase(this@ViewAllRfidTags)
//            eventDao = myEventDataBase.eventDao()
//
//
//            val database = Room.databaseBuilder(this, myEventDataBase::class.java, "Event_database").build()
//            val allData = database.eventDao().getAllData()
//            allData.forEach {
//               Log.d("eeeee",it.rfidNo)
//            }
//
//        } catch (e:Exception){
//            Log.d("Exception",e.toString())
//        }




        rfidList = arrayListOf()
        rfidList = intent.getSerializableExtra(Cons.RFID) as ArrayList<RfidTag>

        //val list = rfidList.distinct()
        val list = DataStorage.dataList.distinct()
        if (list.isEmpty()){
            binding.tvNoDataFound.isVisible= true
        } else {
            getAllScannedRfidStatusAdapter = AllScannedRfidStatusAdapter()
            getAllScannedRfidStatusAdapter.setItems(list)
            binding.rfidTagList.adapter = getAllScannedRfidStatusAdapter
            binding.tvNoDataFound.isVisible= false
        }
    }
}