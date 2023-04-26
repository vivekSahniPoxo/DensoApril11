package com.example.denso.dispatch


import DispatchedItemDetailsAdapter
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.denso.utils.NetworkResult
import com.densowave.scannersdk.Barcode.BarcodeDataReceivedEvent
import com.densowave.scannersdk.Barcode.BarcodeScanner
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.BarcodeDataDelegate
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.databinding.ActivityDispatchBinding
import com.example.denso.dispatch.adapter.AllScannedRfidStatusAdapter
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.dispatch.dispatchmodel.DispatchViewModel
import com.example.denso.dispatch.event_listener.RFIDECheckListener
import com.example.denso.dispatch.model.*
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class DispatchActivity : BaseActivity(),BarcodeDataDelegate,RFIDECheckListener,RFIDDataDelegate {
    lateinit var binding:ActivityDispatchBinding
    private val dispatchViewModel : DispatchViewModel by viewModels()

    lateinit var allScannedRfidStatusAdapter: AllScannedRfidStatusAdapter

    lateinit var progressDialog: ProgressDialog

    lateinit var dispachItemDetailsAdapter: DispatchedItemDetailsAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()
    lateinit var dispatchItem: List<DispatchItem>

    lateinit var dItemList:ArrayList<DispatchItem>

    lateinit var rfidListfromAPi:ArrayList<String>

    lateinit var rfidTagNoFromApiList:ArrayList<BinDispatchDetails.BinDispatchDetailsItem>

    lateinit var matchedRFIdTagList:ArrayList<String>
    lateinit var unMatchedTagsList:ArrayList<String>

    lateinit var listOfUnMatchedTagsList:ArrayList<RfidTag>

    lateinit var barcodeScanner: BarcodeScanner

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true


    lateinit var mRfidTagList:List<RfidTag>

    val context:Context?=null
    lateinit var  rfidListOfObject:ArrayList<RfidTag>

    lateinit var matchedRfidTags:ArrayList<RfidTag>

    lateinit var statusList:ArrayList<RfidDispatchedStatusList>

    private lateinit var matchedRfiDList:ArrayList<String>

    lateinit var matchedSize:ArrayList<String>

    private val allReadTagTagNo = arrayListOf<String>()

    lateinit var temp:ArrayList<String>

    lateinit var pkgGroupName:String
    lateinit var customerName:String
    lateinit var partName:String
    lateinit var statusCode:String
    lateinit var status:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        barcodeScanner = BarcodeScanner(commScanner)

        statusList = arrayListOf()

        pkgGroupName = toString()
        customerName = toString()
        partName = toString()
        statusCode =  toString()
        status = toString()

        matchedRFIdTagList = arrayListOf()
        unMatchedTagsList = arrayListOf()

        rfidListOfObject =  arrayListOf()
        temp = arrayListOf()

        progressDialog = ProgressDialog(this)

        //dispatchItem = listOf()
        dItemList = arrayListOf()

        matchedRfidTags =  arrayListOf()

        rfidListfromAPi  =  arrayListOf()

        rfidTagNoFromApiList = arrayListOf()

        mRfidTagList = listOf()

        matchedRfiDList = arrayListOf()

        listOfUnMatchedTagsList = arrayListOf()

        matchedSize = arrayListOf()

        allScannedRfidStatusAdapter = AllScannedRfidStatusAdapter(mRfidTagList)




        runReadAction()
        runReadActionTag()
        binding.btnStartReading.setOnClickListener {
            runReadActionTag()
        }

        binding.btnClear.setOnClickListener {
            rfidListfromAPi.clear()
            rfidListOfObject.clear()
            rfidTagNoFromApiList.clear()
            binding.autoCompleteTextView.text=""
            temp.clear()
            dispachItemDetailsAdapter.notifyDataSetChanged()

        }
    //        dispatchViewModel.dispatch("01101750000201400 11HA212400-06701H0000600HA212400-06901H0000300HA212400-50201H0000150HA212400-50301H0000150JK212400-05901H0000300")
    //        bindObserverToDispatch()



//        val sIlNo = "26606320000401104 11HA101962-51421A0000700HA101962-91221A0000600"
        //val sIlNo =   binding.autoCompleteTextView.text.trim()
//        val truckNo = sIlNo.substring(0,7)
        //val shipNo = silData.substring(15,2)
//        val customerId = sIlNo.substring(7,8)
//        val partData = sIlNo.removeRange(0,20)
        ///dispatchItem = arrayListOf()

                //lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) {
                    binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                            if (binding.autoCompleteTextView.text.isNotEmpty()) {
                                val list = dItemList
                                Log.d("dItemList", list.toString())
                                //val dispatchitem = DispatchModel(customerId,list, truckNo)
                                dispatchViewModel.dispatch(binding.autoCompleteTextView.text.toString())
                                bindObserverToDispatch()
                            } else{
                                startBarcodeScan()
                            }

                        }
                        override fun afterTextChanged(s: Editable) {

                        }
                    })






//        do dispatchItem = if (partData==Cons.HA){
//                 dItemList.add(DispatchItem(PartNo = partData.substring(0,15), Quatinty = partData.substring(15,7), partData = partData.removeRange(0,22) )) as ArrayList<DispatchItem>
//        } else{
//                    dItemList.add(DispatchItem(PartNo = partData.substring(0,13), Quatinty = partData.substring(13,7),partData = partData.removeRange(0,20) )) as ArrayList<DispatchItem>
//                }
//        while (partData.length>0)
//
//
//        if (binding.autoCompleteTextView.text.isNotEmpty()){
//
//            temp.add("HA297500-24711A")
//            temp.add("0000700")
//             val list = temp as ArrayList<DispatchItem>
//
//            val dispatchitem = DispatchModel(customerId,list, truckNo)
//            dispatchViewModel.dispatch(dispatchitem,)
//            bindObserverToDispatch()
//        }

//        dItemList.add(DispatchItem("HA297500-24711A","0000700",""))
//        val list = dItemList
//        Log.d("dItemList",list.toString())
//        val dispatchitem = DispatchModel(customerId,list, truckNo)
//        dispatchViewModel.dispatch(dispatchitem)
//        bindObserverToDispatch()



        // Service is started in the back ground.
        super.startService()

        binding.apply {
            btnView.setOnClickListener {
                val intent = Intent(this@DispatchActivity, ViewAllRfidTags::class.java)
                val bundle = Bundle()
                bundle.putSerializable(Cons.RFID, (rfidListOfObject+listOfUnMatchedTagsList) as ArrayList<RfidTag>)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            btnConfirmDispatch.setOnClickListener {

//                val createStatus = CreateRfidStatus( (rfidListOfObject+listOfUnMatchedTagsList) as ArrayList<RfidTag>)
               // if (rfidListOfObject.isNotEmpty()) {
                    dispatchViewModel.confirmDispatch((rfidListOfObject) as ArrayList<RfidTag>)
                    bindObserverToCreateRfidTagStatus()
               // } else{
                    //Toast.makeText(this@DispatchActivity,"Please scan tags",Toast.LENGTH_SHORT).show()
                }
            //}

            imBack.setOnClickListener {
                navigateUp()
            }



        }

   // }
    }

    override fun onResume() {
        super.onResume()
        startSession()
        if (nextReadAction == ReadAction.STOP) {
            startBarcodeScan()
        }
    }

    override fun onPause() {
        super.onPause()
        stopBarcodeScan()
    }


    override fun onDestroy() {
        if (scannerConnectedOnCreate && disposeFlg) {
            super.disconnectCommScanner()
        }
        super.onDestroy()
    }

    override fun onRestart() {
        disposeFlg = true
        super.onRestart()
    }


    /**
     * When Home button is pressed
     */
     override fun onUserLeaveHint() {
        // Tag reading end
        if (scannerConnectedOnCreate) {
            // When the tag reading is started
            if (nextReadAction == ReadAction.STOP) {
                // Tag loading end
                runReadActionTag()
                disposeFlg = false
            }
        }
    }

    /**
     * Key event
     *
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                // Tag reading end
                if (scannerConnectedOnCreate) {
                    // When the tag reading is started
                    if (nextReadAction == ReadAction.STOP) {
                        // Tag loading end
                        runReadActionTag()
                    }

                    // Remove delegate
                    super.getCommScanner()!!.rfidScanner.setDataDelegate(null)
                }
                handler = null
                disposeFlg = false
                finish()
                return true
            }
        }
        return false
    }






    private fun bindObserverToDispatch(){
      dispatchViewModel.dispatchResponseLiveData.observe(this, Observer {
          hideProgressbar()
          when(it){
              is NetworkResult.Success->{
                  Log.d("data",it.data.toString())
                  if (it.data?.isEmpty() == true){
                      binding.tvNoDataFound.isVisible = true
                      binding.btnClear.alpha = 0.3F
                      startBarcodeScan()
                  }else {
                      dispachItemDetailsAdapter = DispatchedItemDetailsAdapter(this, it.data as List<BinDispatchDetails.BinDispatchDetailsItem>)
                      binding.listOfItem.adapter = dispachItemDetailsAdapter
                      binding.tvNoDataFound.isVisible = false
                      stopBarcodeScan()
                      binding.btnClear.alpha = 10F
                      binding.btnClear.setTextColor(getColor(R.color.white))
                      rfidTagNoFromApiList = it.data



                      pkgGroupName = it.data[0].groupName
                      customerName = it.data[0].customerName
                      partName = it.data[0].partName
                      for(i in it.data[1].rfidNumber) {
                          statusCode = i.statusCode.toString()
                      }

                      for (currentStatus in it.data[1].rfidNumber){
                          status = currentStatus.status
                          statusList.add(RfidDispatchedStatusList(currentStatus.status))
                      }

                      // getting all rfid tag no from dispatch details api
                      for(rfidFromApi in rfidTagNoFromApiList[0].rfidNumber) {
                          Log.d("outerLine", rfidFromApi.rfidTagNo)
                          rfidListfromAPi.add(rfidFromApi.rfidTagNo)
                          rfidListOfObject.add(RfidTag(rfidFromApi.rfidTagNo,statusCode,partName,pkgGroupName,customerName))
                      }


                  }

                               for (i in it.data[1].rfidNumber) {
                                   temp.add(i.rfidTagNo)
                                   Log.d("rfidVivek", i.rfidTagNo)
                                  binding.tvTotalTagList.text = temp.size.toString()
                                   Log.d("size",temp.size.toString())
                              binding.tvTotalTagListTwo.text = temp.size.toString()
                               }
                  binding.tvTotalTagListTwo.text = temp.size.toString()

              }
              is NetworkResult.Error->{
                  Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
              }
              is NetworkResult.Loading->{
                 showProgressbar()
              }
          }
      })
    }


    /**
     * Since it is called back after reading bar code,
     * Add the import result to list.
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onBarcodeDataReceived(commScanner: CommScanner?, barcodeDataReceivedEvent: BarcodeDataReceivedEvent?) {
        if (barcodeDataReceivedEvent != null) {
            val barcodeDataList = barcodeDataReceivedEvent.barcodeData
            if (barcodeDataList != null && barcodeDataList.size > 0) {
                handler?.post {
                    for (barcodeData in barcodeDataList) {
                        var text = """${barcodeData.symbologyDenso}(${barcodeData.symbologyAim})""".trimIndent()
                        try {
                            text += String(barcodeData.data)
                            Log.d("text",text)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val txt = text.removeRange(0,6)
                        binding.autoCompleteTextView.text = txt
                    }

                }
            }
        }
    }

    /**
     * Execute the loading action
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun runReadAction() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                // Clear tag
                binding.autoCompleteTextView.clearFocus()

                // Start scanning barcode
                startBarcodeScan()
            }
            ReadAction.STOP ->                 // Stop scanning barcode
                stopBarcodeScan()
            else -> {

            }
        }

        // Set next reading action
        // Switch the previous reading action to STOP is it was STARTed, and to START if it was STOPped.
        nextReadAction = if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

        // For the buttons, set the name of the action to be executed next
        binding.btnStartReading.text = nextReadAction.toResourceString(resources)
    }

    /**
     * Get the instance of bar code scanner.
     */
    private fun startSession() {
        if (commScanner != null) {
            barcodeScanner = commScanner!!.barcodeScanner
        }
    }


    /**
     * Stop scanning barcode
     */
    private fun stopBarcodeScan() {
        if (barcodeScanner != null) {
            try {
                barcodeScanner.closeReader()
                barcodeScanner.setDataDelegate(null)
                startTagReading()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }




    /**
     * Start scanning barcode
     */
    private fun startBarcodeScan() {
        startSession()
        if (barcodeScanner != null) {
            try {
                // Set listener
                barcodeScanner.setDataDelegate(this)

                // Start scanning
                barcodeScanner.openReader()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onRfidListener(item: BinDispatchDetails.BinDispatchDetailsItem) {
        for(i in item.rfidNumber)
     Log.d("rfidVivekOverride",i.rfidTagNo)

    }

    override fun onRFIDDataReceived(p0: CommScanner?, p1: RFIDDataReceivedEvent) {
        // Control between threads
        handler!!.post {
            readData(p1)
        }
    }


    private fun readData(rfidDataReceivedEvent: RFIDDataReceivedEvent) {

            for (i in rfidDataReceivedEvent.rfidData.indices) {
                var data = ""
                val uii = rfidDataReceivedEvent.rfidData[i].uii
                for (loop in uii.indices) {
                    data += String.format("%02X ", uii[loop]).trim { it <= ' ' }
                }

                  allReadTagTagNo.add(data)

                if (rfidListfromAPi.contains(data)){
                    val result = rfidListOfObject.find { it.rfidTagNo==data }
                    //result?.status = "dispatch"
                    result?.status = 2.toString() // getting  rfid from dispatch details api
                    binding.noReadTags.text = "${rfidListOfObject.size}/"

                    Log.d("kwownitem",result.toString())
                } else{
                    if(!unMatchedTagsList.contains(data)) {
                        listOfUnMatchedTagsList.add(RfidTag(data,statusCode,partName,pkgGroupName,customerName))
                         unMatchedTagsList.add(data)  //Red
                        val unMatchList = unMatchedTagsList.distinct()
//                        unMatchedTagsList.add("0") // Rfid is not found in dispatch details api
                        binding.noInvalidTags.text = "${unMatchList.size}/"
                        Log.d("unKwownItem",data)
                    }
                }
                 val allTags = allReadTagTagNo.distinct()
                binding.noReadTags.text = "${allTags.size}"
               // binding.tvTotalTagListTwo.text = allTags.size.toString()
            }
        }


    /**
     * Execute the loading action
     */
    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                binding.apply {
                    btnClear.isEnabled = false
                    btnClear.setTextColor(getColor(R.color.white))
                }

                    // Tag reading starts
                    if (scannerConnectedOnCreate) {
                        try {
                            super.getCommScanner()!!.rfidScanner.openInventory()
                        } catch (e: Exception) {
                            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
                            e.printStackTrace()
                        }
                    }
                }

            ReadAction.STOP -> {
                // Tag loading end
                if (scannerConnectedOnCreate) {
                    try {
                        super.getCommScanner()!!.rfidScanner.close()
                    } catch (e: Exception) {
                        super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
                        e.printStackTrace()
                    }
                }
                binding.apply {
                    btnClear.isEnabled = true
                    btnClear.alpha = 0.2F
                    btnClear.setTextColor(getColor(R.color.text_default))
                }

            }
        }

        // Set the next reading action
        // Switch the previous reading action to STOP is it was STARTed, and to START if it was STOPped.
        nextReadAction =
            if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

        // For the buttons, set the name of the action to be executed next
       // val readToggle = findViewById<View>(R.id.button_read_toggle) as Button
        binding.btnStartReading.text = nextReadAction.toResourceString(resources)
    }


    private fun bindObserverToCreateRfidTagStatus(){
        dispatchViewModel.confirmDispatchLiveData.observe(this, Observer {
            hideProgressbar()
            when(it){
             is NetworkResult.Success->{
                 Toast.makeText(this,"SuccessFully Confirmed Dispatched",Toast.LENGTH_SHORT).show()
                 rfidListfromAPi.clear()
                 rfidListOfObject.clear()
                 rfidTagNoFromApiList.clear()
                 binding.autoCompleteTextView.text=""
                 binding.noInvalidTags.text = ""
                 binding.noReadTags.text=""
                 temp.clear()
                 dispachItemDetailsAdapter.notifyDataSetChanged()
             }

                is NetworkResult.Error->{
                    Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()}

                is NetworkResult.Loading->{
                    showProgressbar()
                } } })
    }

    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideProgressbar(){
        progressDialog.hide()
    }

    private fun startTagReading(){
        scannerConnectedOnCreate = super.isCommScanner()

        if (scannerConnectedOnCreate) {
            try {
                super.getCommScanner()!!.rfidScanner.setDataDelegate(this)
            } catch (e: Exception) {
                // Failed to register data listener.
                super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            }
        } else {
            // When SP1 is not found, display the error message.
            super.showMessage(getString(R.string.E_MSG_NO_CONNECTION))
        }

        // Service is started in the back ground.
        super.startService()
    }


    /**
     * Move to the upper level at the time of screen transition
     */
    private fun navigateUp() {
        // Tag reading end
        if (scannerConnectedOnCreate) {
            // When the tag reading is started
            if (nextReadAction == ReadAction.STOP) {
                // Tag loading end
                runReadActionTag()
            }

            // Remove delegate
            super.getCommScanner()!!.rfidScanner.setDataDelegate(null)
        }
        handler = null
        disposeFlg = false

        // Although there is such embedded navigation function "Up Button" in Android,
        // since it doesn't meet the requirement due to the restriction on UI, transition the the screen using button events.
        val intent = Intent(application, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        // Stop the Activity because it becomes unnecessary since the parent Activity is returned to.
        finish()
    }


}




