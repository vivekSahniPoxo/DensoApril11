package com.example.denso.dispatch


import DispatchedItemDetailsAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.example.denso.dispatch.dispatchmodel.TempDispatch

import com.example.denso.dispatch.event_listener.RFIDECheckListener
import com.example.denso.dispatch.model.*
import com.example.denso.dispatch.roomdb.database.EventDao
import com.example.denso.dispatch.roomdb.database.EventDatabase

import com.example.denso.utils.*
import com.example.denso.utils.DataStorage.dataList
import com.example.denso.utils.sharePreference.SharePref

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@AndroidEntryPoint
class DispatchActivity : BaseActivity(),BarcodeDataDelegate,RFIDECheckListener,RFIDDataDelegate {
    val processedGroupSetForRfidListOfObject = mutableSetOf<Pair<String, String>>()
    lateinit var uniqueCtnSet: HashSet<String>
    var toastDisplayed = false
    var checkSilNo = arrayListOf<String>()

    val isScannedRfidNumber = mutableSetOf<String>()
    val ifSingleSilHaveMoretheOneData = arrayListOf<String>()
    lateinit var eventDao: EventDao
    val foundList = arrayListOf<String>() // List to store found RFID numbers
    val viewModelScope = CoroutineScope(Dispatchers.Main)
    var txt =""
    var tempRFidList = arrayListOf<String>()
    val rfidNumberList = arrayListOf<TempDispatch.TempItem.RfidNumber>()
    val  requestList = arrayListOf<TempDispatch.TempItem>()
    val silNo = arrayListOf<MatchingConditionDataClass>()

    val matchedRfidSet = mutableSetOf<String>()
     val isAlreadySilNo = arrayListOf<String>()
    private val unmatchedRfidSet = mutableSetOf<String>()
    val processedGroups = arrayListOf<String>()
    private var lastExecutionTime: Long = 0
    private val THROTTLE_INTERVAL = 1000 // Adjust the interval as needed (in milliseconds)

    var matchingCountInGroup = 0
    val foundRfidList = mutableSetOf<String>()
    val inRfidList = mutableSetOf<String>()
    val matchRFidListInGroupCount = mutableSetOf<String>()
    var tempCtnValue = mutableSetOf<String>()


    val matchedCountMap = mutableMapOf<Pair<String, String>, Int>()
    val scannedRfid =  mutableSetOf<String>()

    var index = 0
  //  val database = Room.databaseBuilder(this, LocaleDataBase::class.java, "LocaleDataBase").build()

    lateinit var sharePref: SharePref

    lateinit var viewFoundItem:ArrayList<RfidTag>
    lateinit var getAllScannedRfidStatusAdapter: AllScannedRfidStatusAdapter


    var getCtn = 0
    lateinit var temprfidModel:ArrayList<TemprfidModel>


    lateinit var binding:ActivityDispatchBinding
    private val dispatchViewModel : DispatchViewModel by viewModels()
   // private val localDataBaseViewModel:LocalDataBaseViewModel by viewModels()

    lateinit var allScannedRfidStatusAdapter: AllScannedRfidStatusAdapter

    lateinit var progressDialog: ProgressDialog

    lateinit var dispachItemDetailsAdapter: DispatchedItemDetailsAdapter
   lateinit var dialogTag:Dialog

    private lateinit var linearLayoutManager: LinearLayoutManager

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()
    lateinit var dispatchItem: List<DispatchItem>

    lateinit var dItemList:ArrayList<DispatchItem>

    lateinit var rfidListfromAPi:ArrayList<String>

    lateinit var rfidTagNoFromApiList:ArrayList<BinDispatchDetails.BinDispatchDetailsItem>

    lateinit var getAllDataFromApi:ArrayList<BinDispatchDetails.BinDispatchDetailsItem>

    lateinit var matchedRFIdTagList:ArrayList<String>
    lateinit var unMatchedTagsList:ArrayList<String>

    lateinit var listOfUnMatchedTagsList:ArrayList<RfidTag>

    lateinit var barcodeScanner: BarcodeScanner

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true


    lateinit var mRfidTagList:List<RfidTag>

    val context:Context?=null
    lateinit var  rfidListOfObject:ArrayList<TempDispatch.TempItem>

    lateinit var matchedRfidTags:ArrayList<RfidTag>

    lateinit var scannedRFId:ArrayList<RFIDNo>

    lateinit var statusList:ArrayList<RfidDispatchedStatusList>

    private lateinit var matchedRfiDList:ArrayList<String>

    lateinit var matchedSize:ArrayList<String>

    private val allReadTagTagNo = arrayListOf<String>()

    lateinit var temp:ArrayList<String>

    lateinit var gettingRFidFromApi:ArrayList<String>
    lateinit var matchedList:kotlin.collections.ArrayList<String>

    lateinit var pkgGroupName:String
    lateinit var customerName:String
    lateinit var partName:String
    lateinit var statusCode:String
    lateinit var status:String

    lateinit var allRFIDNumberFromApi: ArrayList<AllRFIDNumberFromApi>

    lateinit var groupwiseList:ArrayList<GroupWiseDataModel>
    lateinit var AllGroupName:ArrayList<AllGroupNameModel>

    lateinit var tempList:ArrayList<String>
    lateinit var invalidRfidList:ArrayList<String>

    lateinit var rfidList:ArrayList<String>
    lateinit var myEventDataBase:EventDatabase

    lateinit var rfidListTest:ArrayList<String>
    var rfidNo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tempList = arrayListOf()
        rfidList = arrayListOf()
        sharePref = SharePref()
        rfidListTest = arrayListOf()

        uniqueCtnSet = hashSetOf()
        myEventDataBase = EventDatabase.getDatabase(this@DispatchActivity)
        eventDao = myEventDataBase.eventDao()

        binding.btnClear.isEnabled = true

        barcodeScanner = BarcodeScanner(commScanner)

        temprfidModel = arrayListOf()

        invalidRfidList = arrayListOf()

        groupwiseList = arrayListOf()

        AllGroupName  = arrayListOf()

        viewFoundItem  = arrayListOf()

        gettingRFidFromApi = arrayListOf()
        allRFIDNumberFromApi = arrayListOf()

        matchedList = arrayListOf()

        statusList = arrayListOf()
        scannedRFId  =  arrayListOf()

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
        getAllDataFromApi = arrayListOf()
       dispachItemDetailsAdapter = DispatchedItemDetailsAdapter(this,getAllDataFromApi)
//        binding.listOfItem.adapter = dispachItemDetailsAdapter


        mRfidTagList = listOf()

        matchedRfiDList = arrayListOf()

        listOfUnMatchedTagsList = arrayListOf()

        matchedSize = arrayListOf()

        allScannedRfidStatusAdapter = AllScannedRfidStatusAdapter()

      /*  rfidListTest.add("453030373033303030303032")
        rfidListTest.add("453030373033303030303035")
        rfidListTest.add("453030373033303030303036")
        rfidListTest.add("453030373033303030303037")
        rfidListTest.add("453030373033303030303038")
        rfidListTest.add("453030373033303030303039")
        rfidListTest.add("453030373033303030303130")
        rfidListTest.add("453030373033303030303131")
        rfidListTest.add("453030373033303030303132")
        rfidListTest.add("453030373033303030303133")
        rfidListTest.add("453030373033303030303134")
        rfidListTest.add("453030373033303030303136")

        rfidListTest.add("453030373033303030303137")
        rfidListTest.add("453030373033303030303138")
        rfidListTest.add("453030373033303030303139")
        rfidListTest.add("453030373033303030303230")
        rfidListTest.add("453030373033303030303231")*/


        try {
            sharePref = SharePref()
            val savedBaseUrl = sharePref.getData("baseUrl")
            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = savedBaseUrl
                Log.d("baseURL", savedBaseUrl)
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }

        if (dataList.isNotEmpty()){
            dataList.clear()
        }



        runReadAction()
        runReadActionTag()
        binding.btnStartReading.setOnClickListener {
            stopBarcodeScan()
            runReadActionTag()

        }



        binding.btnClear.setOnClickListener {
            rfidListfromAPi.clear()
            rfidListOfObject.clear()
            getAllDataFromApi.clear()
            dispachItemDetailsAdapter.clearItems()
            dispachItemDetailsAdapter.refreshAdapter()
            rfidTagNoFromApiList.clear()
            binding.autoCompleteTextView.text=""
            binding.noInvalidTags.text = ""
            binding.noReadTags.text=""
            binding.tvCustName.text = ""
            binding.tvLocation.text = ""
            binding.tvTotalTagListTwo.text = ""
            binding.tvTotalTagList.text = ""
            temp.clear()
            binding.tvSlash.text = ""
            dispachItemDetailsAdapter.notifyDataSetChanged()

//            rfidListfromAPi.clear()
//            rfidListOfObject.clear()
//            rfidTagNoFromApiList.clear()
//            binding.autoCompleteTextView.text=""
//            binding.tvCustName.text = ""
//            binding.tvLocation.text = ""
//            binding.noReadTags.text = ""
//            binding.noInvalidTags.text = ""
//            binding.tvTotalTagListTwo.text = ""
//            temp.clear()
//
//            dispachItemDetailsAdapter.clearItems()
//            dispachItemDetailsAdapter.notifyDataSetChanged()

        }


        super.startService()



        binding.apply {
            btnView.setOnClickListener {
                viewRfidStatus()
//                val intent = Intent(this@DispatchActivity, ViewAllRfidTags::class.java)
//                val bundle = Bundle()
//                bundle.putSerializable(Cons.itemRfid,"false")
//                bundle.putSerializable(Cons.RFID, (DataStorage.dataList) as ArrayList<RfidTag>)
//                intent.putExtras(bundle)
//                startActivity(intent)
            }

            btnConfirmDispatch.setOnClickListener {
                var gson = Gson()
                var jsonString = gson.toJson(rfidListOfObject)
//                Log.d("kjjj",jsonString)
                try{

                    val currentDateTime: java.util.Date = java.util.Date()

                writeToFileExternal(applicationContext,"RequestBody${currentDateTime.toString()}.txt",jsonString)
                } catch (e:Exception){

                }
                dialogForTag()
              //  writeToFileExternal("rfidListOfObject",rfidListOfObject.toString())


                }


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



    private fun getDispatchbindObserver(){
        val activity = this@DispatchActivity
        dispatchViewModel.getDispatchItemLiveData.observe(this, Observer { it ->
            if (activity.isDestroyed || activity.isFinishing) {
                return@Observer
            }
           // hideProgressbar()

            binding.progressBar.isVisible = false
            applicationContext?.let { context ->
                try {
            when(it){
                is NetworkResult.Success->{

                    binding.autoCompleteTextView.text = ""
                    txt = ""

                    if (it.data?.isEmpty() == true){
                        if (!isFinishing && !isDestroyed) {
                            Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                        }
//                      binding.tvNoDataFound.isVisible = true
                        binding.btnClear.alpha = 0.3F
                        startBarcodeScan()
                    }else if (dataList.size<=20000)  {
                        //Log.d("it.data.respons", it.data.toString())


                        try {
                            val partNo: List<String>? = it.data?.map { it.partNo }

                            val uniqueRfidNumbers = HashSet<String>()
                            for (i in it.data?.get(0)?.rfidNumber!!) {
                                // statusCode = i.statusCode.toString()
                                if (!uniqueRfidNumbers.contains(i.rfidTagNo)) {
                                    uniqueRfidNumbers.add(i.rfidTagNo.toString())
                                    dataList.add(
                                        RfidTag(
                                            i.rfidTagNo.toString(),
                                            partNo?.joinToString(", ") ?: "",
                                            i.status.toString()
                                        )
                                    )

                                }
                            }

                            for (currentStatus in it.data[1].rfidNumber) {
                                status = currentStatus.status.toString()
                                statusList.add(RfidDispatchedStatusList(currentStatus.status.toString()))
                            }

                        } catch (e: Exception) {

                        }




                        try {
                            it.data?.forEach {

                                getAllDataFromApi.add(
                                    BinDispatchDetails.BinDispatchDetailsItem(
                                        it.ctn,
                                        it.customerAddress,
                                        it.customerCode,
                                        it.customerName,
                                        it.groupName,
                                        it.lotSize,
                                        it.partName,
                                        it.partNo,
                                        "it.pkgPartNo",
                                        it.rfidNumber,
                                        "it.shipQty",
                                        it.weight,
                                        it.silNo,
                                        false,
                                        0,
                                        0,
                                        0,
                                        0

                                    )
                                )


                                val ctnList = ArrayList<Int>()
                                val distinctGroupNames = HashSet<String>()

                                for (obj in getAllDataFromApi) {
                                    if (obj.partNo !in distinctGroupNames || obj.silNo !in distinctGroupNames) {
                                        ctnList.add(obj.ctn)
                                        distinctGroupNames.add(obj.partNo)
                                        distinctGroupNames.add(obj.silNo)
                                    }
                                }


                                val totalCtn = ctnList.sum()
                                getCtn = totalCtn // Update getCtn with the totalCtn value

                                // binding.tvTotalTagList.text = "/$getCtn"
                                binding.tvTotalTagList.text = getCtn.toString()
                                binding.tvTotalTagListTwo.text = getCtn.toString()
//                            Log.d("totalallala", dispachItemDetailsAdapter.totalCtn.toString())

                                dispachItemDetailsAdapter = DispatchedItemDetailsAdapter(
                                    this,
                                    getAllDataFromApi as ArrayList<BinDispatchDetails.BinDispatchDetailsItem>
                                )
                                binding.listOfItem.adapter = dispachItemDetailsAdapter
                                dispachItemDetailsAdapter.refreshAdapter()
//                                Log.d("getAllDataFromApi", getAllDataFromApi.toString())
                            }
                        } catch (e: Exception) {
                          //  Log.d("exception", e.toString())

                        }
                        // }
                        val tempDispatch = TempDispatch()

                        // val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
                        val storedValue = sharePref.getData(Cons.userId)

                        try {
                            for (item in getAllDataFromApi) {
                                var found = false

                                for (tempItem in tempDispatch) {
                                    if (tempItem.pkgGroupName == item.groupName) {
                                        // Check if RFID number already exists in TempItem
                                        val existingRfidNumbers = tempItem.rfidNumber.map { it.rfidNumber.toString() }
                                        val newRfidNumbers = item.rfidNumber.map { it.rfidTagNo.toString() }
                                        tempItem.rfidNumber = mutableListOf()

//                                        for (rfidNumber in newRfidNumbers) {
//                                            if (!existingRfidNumbers.contains(rfidNumber)) {
//                                                (tempItem.rfidNumber as MutableList<TempDispatch.TempItem.RfidNumber>).add(TempDispatch.TempItem.RfidNumber(rfidNumber, "0"))
//                                            }
//                                        }

                                        found = true
                                        break
                                    }
                                }

                                // If groupName doesn't exist, create a new TempItem
                                if (!found) {
                                    val tempItem = TempDispatch.TempItem(
                                        item.ctn,
                                        storedValue.toString(),
                                        item.customerCode,
                                        0,
                                       item.ctn-item.found,
                                        item.partNo,
                                        item.groupName,
                                        item.rfidNumber.map { rfid ->
                                            TempDispatch.TempItem.RfidNumber(
                                                rfid.rfidTagNo.toString(),
                                                "0"
                                            )
                                        },
                                        item.silNo,

                                    )
                                    tempDispatch.add(tempItem)
                                    rfidListOfObject.add(tempItem)
                                }
                            }
                        } catch (e: Exception) {
                            // Handle the exception appropriately
                            e.printStackTrace()
                        }

//                        try {
//                            for (item in getAllDataFromApi) {
//                                var found = false
//                                // Check if the groupName already exists in TempDispatch
//                                for (tempItem in tempDispatch) {
//                                    if (tempItem.pkgGroupName == item.groupName) {
//                                        // Update the existing TempItem with rfidNumber data
//                                        tempItem.rfidNumber =
//                                            tempItem.rfidNumber + item.rfidNumber.map { rfid ->
//                                                TempDispatch.TempItem.RfidNumber(
//                                                    rfid.rfidTagNo.toString(),
//                                                    "0"
//                                                )
//                                            }
//                                        found = true
//                                        break
//                                    }
//                                }
//                                // If the groupName does not exist, create a new TempItem and add it to TempDispatch
//                                if (!found) {
//                                    val tempItem = TempDispatch.TempItem(
//                                        item.ctn,
//                                        storedValue.toString(), // Set the createdby value accordingly
//                                        item.customerCode, // Set the customer value accordingly
//                                        0, // Set the found value accordingly
//                                        item.rfidNumber.size, // Set the notFound value accordingly
//                                        item.partNo, // Set the partNo value accordingly
//                                        item.groupName,
//                                        item.rfidNumber.map { rfid ->
//                                            TempDispatch.TempItem.RfidNumber(
//                                                rfid.rfidTagNo.toString(),
//                                                "0"
//                                            )
//                                        },
//                                        item.silNo,
//                                        getCtn
//                                    )
//                                    tempDispatch.add(tempItem)
//                                    rfidListOfObject.add(tempItem)
//                                }
//                            }
//
//                        } catch (e:Exception){
//
//                        }







                        binding.tvNoDataFound.isVisible = false
                        // stopBarcodeScan()
                        binding.btnClear.alpha = 10F
                        binding.btnClear.setTextColor(getColor(R.color.white))

                        // rfidTagNoFromApiList = it.data


                        try {
                            it.data?.forEach {
                                for (rfid in it.rfidNumber)
                                    groupwiseList.add(
                                        GroupWiseDataModel(
                                            it.groupName,
                                            it.ctn.toString(),
                                            rfid.rfidTagNo.toString(),
                                            rfid.status.toString()
                                        )
                                    )

                          //      AllGroupName.add(AllGroupNameModel(it.groupName, it.ctn))


                            }

                            pkgGroupName = it.data?.get(0)?.groupName.toString()
                            customerName = it.data?.get(0)?.customerName.toString()
                            binding.tvCustName.text = it.data?.get(0)?.customerName.toString()
                            binding.tvLocation.text = it.data?.get(0)?.customerAddress
                            partName = it.data?.get(0)?.partName.toString()


                        } catch (e:Exception){

                        }

                    } else if (dataList.size>20000){
                        Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    } else{
                        Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }









                    try {
                        for (i in it.data?.get(0)?.rfidNumber!!) {
                            temp.add(i.rfidTagNo.toString())
//
                        }
                    } catch (e:Exception){

                    }

                }
                is NetworkResult.Error->{
                    if (!isFinishing && !isDestroyed) {
                        if (it.message.toString() == "Expected BEGIN_ARRAY but was STRING at line 1 column 1 path \$") {
                            Snackbar.make(
                                binding.root,
                                "There is no Related Data For This Silnumber",
                                Snackbar.LENGTH_LONG
                            ).show()

                            binding.autoCompleteTextView.text = ""
                        } else {
                            binding.autoCompleteTextView.text = ""
                            if (!isFinishing && !isDestroyed) {
                                Snackbar.make(
                                    binding.root,
                                    it.message.toString(),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                        }

                            //Log.d("errorMassage", it.message.toString())
                    }
                }
                is NetworkResult.Loading->{

                    if (!isFinishing && !isDestroyed) {

                        runOnUiThread {
                            Snackbar.make(binding.root, "Please Wait..", Snackbar.LENGTH_SHORT).show()
                            binding.progressBar.isVisible = true

                        }

                    }
                }
            }
                } catch (e: Exception) {
                    //Log.e("Exception", "Error in UI operations: ${e.message}")
                }
            }
        })
    }




    private fun bindObserverToDispatch(){
      dispatchViewModel.dispatchResponseLiveData.observe(this, Observer { it ->
          binding.progressBar.isVisible = false
          when(it){
              is NetworkResult.Success-> {
//
                  binding.autoCompleteTextView.text = ""

//                  val handler = Handler(Looper.getMainLooper())
//                  handler.post {

                      if (it.data?.isNullOrEmpty() == true) {

                          Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
//                      binding.tvNoDataFound.isVisible = true
                          binding.btnClear.alpha = 0.3F
                          startBarcodeScan()
                         // clearApplicationCache(this)


                      } else if (dataList.size<=20000) {
//                          Log.d("it.data.respons", it.data.toString())


                          try {
                              val partNo: List<String>? = it.data?.map { it.partNo }
                              val uniqueRfidNumbers = HashSet<String>()
                              for (i in it.data?.get(0)?.rfidNumber!!) {
                                  // statusCode = i.statusCode.toString()
                                  if (!uniqueRfidNumbers.contains(i.rfidTagNo)) {
                                      uniqueRfidNumbers.add(i.rfidTagNo.toString())
                                     val rfidTag = RfidTag(i.rfidTagNo.toString(), partNo?.joinToString(", ") ?: "",i.status.toString())
                                     // localDataBaseViewModel.addAllRfidTag(rfidTag)

                                      dataList.add(rfidTag)
                                  
//                                      DataStorage.dataList.add(
//                                          RfidTag(
//                                              i.rfidTagNo.toString(),
//                                              i.status.toString()
//                                          )
//                                      )

                                  }
                              }

                              for (currentStatus in it.data[1].rfidNumber) {
                                  status = currentStatus.status.toString()
                                  statusList.add(RfidDispatchedStatusList(currentStatus.status.toString()))
                              }

                          } catch (e: Exception) {

                          }





                          try {
                              it.data.forEach {

                                  getAllDataFromApi.add(
                                      BinDispatchDetails.BinDispatchDetailsItem(
                                          it.ctn,
                                          it.customerAddress,
                                          it.customerCode,
                                          it.customerName,
                                          it.groupName,
                                          it.lotSize,
                                          it.partName,
                                          it.partNo,
                                          "it.pkgPartNo",
                                          it.rfidNumber,
                                          "it.shipQty",
                                          it.weight,
                                          it.silNo,
                                          false,
                                          0,
                                          0,
                                          0,
                                          0

                                      )
                                  )


                                  val ctnList = ArrayList<Int>()
                                  val distinctGroupNames = HashSet<String>()

                                  for (obj in getAllDataFromApi) {
                                      if (obj.partNo !in distinctGroupNames || obj.silNo !in distinctGroupNames) {
                                          ctnList.add(obj.ctn)
                                          distinctGroupNames.add(obj.partNo)
                                          distinctGroupNames.add(obj.silNo)
                                      }
                                  }


                                  val totalCtn = ctnList.sum()
                                  getCtn = totalCtn // Update getCtn with the totalCtn value

                                  // binding.tvTotalTagList.text = "/$getCtn"
                                  binding.tvTotalTagList.text = getCtn.toString()
                                  binding.tvTotalTagListTwo.text = getCtn.toString()
                          //                            Log.d("totalallala", dispachItemDetailsAdapter.totalCtn.toString())

                                  dispachItemDetailsAdapter = DispatchedItemDetailsAdapter(
                                      this,
                                      getAllDataFromApi as ArrayList<BinDispatchDetails.BinDispatchDetailsItem>
                                  )
                                  binding.listOfItem.adapter = dispachItemDetailsAdapter
                                  dispachItemDetailsAdapter.refreshAdapter()
                          //                                  Log.d("getAllDataFromApi", getAllDataFromApi.toString())
                              }
                          } catch (e: Exception) {
                            //  Log.d("exception", e.toString())

                          }
                          // }
                          val tempDispatch = TempDispatch()


                          val storedValue = sharePref.getData(Cons.userId)

                          try {
                              for (item in getAllDataFromApi) {
                                  var found = false
                                  // Check if the groupName already exists in TempDispatch
                                  for (tempItem in tempDispatch) {
                                      if (tempItem.pkgGroupName == item.groupName) {
                                          // Update the existing TempItem with rfidNumber data
                                          tempItem.rfidNumber =
                                              tempItem.rfidNumber + item.rfidNumber.map { rfid ->
                                                  TempDispatch.TempItem.RfidNumber(
                                                      rfid.rfidTagNo.toString(),
                                                      "0"
                                                  )
                                              }
                                          found = true
                                          break
                                      }
                                  }
                                  // If the groupName does not exist, create a new TempItem and add it to TempDispatch
                                  if (!found) {
                                      val tempItem = TempDispatch.TempItem(
                                          item.ctn,
                                          storedValue.toString(), // Set the createdby value accordingly
                                          item.customerCode, // Set the customer value accordingly
                                          0, // Set the found value accordingly
                                          item.ctn, // Set the notFound value accordingly
                                          item.partNo, // Set the partNo value accordingly
                                          item.groupName,
                                          item.rfidNumber.map { rfid ->
                                              TempDispatch.TempItem.RfidNumber(
                                                  rfid.rfidTagNo.toString(),
                                                  "0",

                                              )
                                          },
                                          item.silNo,
                                      )
                                      tempDispatch.add(tempItem)
                                      rfidListOfObject.add(tempItem)
                                  }
                              }

                          } catch (e: Exception) {

                          }







                          binding.tvNoDataFound.isVisible = false
                          // stopBarcodeScan()
                          binding.btnClear.alpha = 10F
                          binding.btnClear.setTextColor(getColor(R.color.white))

                          // rfidTagNoFromApiList = it.data


                          try {
                              it.data?.forEach { dataItem ->
                                  for (rfid in dataItem.rfidNumber) {
                                      groupwiseList.add(
                                          GroupWiseDataModel(
                                              dataItem.groupName,
                                              dataItem.ctn.toString(),
                                              rfid.rfidTagNo.toString(),
                                              rfid.status.toString()
                                          )
                                      )
                                  }

                                  AllGroupName.add(AllGroupNameModel(dataItem.groupName, dataItem.ctn))
                              }

//                              it.data?.forEach {
//                                  for (rfid in it.rfidNumber)
//                                      groupwiseList.add(
//                                          GroupWiseDataModel(
//                                              it.groupName,
//                                              it.ctn.toString(),
//                                              rfid.rfidTagNo.toString(),
//                                              rfid.status.toString()
//                                          )
//                                      )
//
//                                  AllGroupName.add(AllGroupNameModel(it.groupName, it.ctn))
//
//
//                              }

                              pkgGroupName = it.data?.get(0)?.groupName.toString()
                              customerName = it.data?.get(0)?.customerName.toString()
                              binding.tvCustName.text = it.data?.get(0)?.customerName.toString()
                              binding.tvLocation.text = it.data?.get(0)?.customerAddress
                              partName = it.data?.get(0)?.partName.toString()


                          } catch (e: Exception) {

                          }

                      } else if (dataList.size>=20000){
                          Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                          binding.progressBar.isVisible = false
                      } else{

                      }









                      try {
                          for (i in it.data?.get(0)?.rfidNumber!!) {
                              temp.add(i.rfidTagNo.toString())
//
                          }
                      } catch (e: Exception) {

                      }

                 // }
              }
              is NetworkResult.Error->{

                      try {
                          val errorMessage = it.message ?: "Unknown error"
                          // Log the error
                          Log.e("API Error", errorMessage)

                          // Check if the error message indicates the absence of related data
                          if (errorMessage.contains("Expected BEGIN_ARRAY but was STRING")) {
                              // Inform the user about the absence of related data
                              toast("There is no related data to this SilNo")

                          } else {
                              // Inform the user about the general error
                            toast(errorMessage)
                          }
                      } catch (e: Exception) {
                          // Log any exceptions that might occur during handling
                          Log.e("API Error", "Error processing response: ${e.message}")
                          // Inform the user about a generic error
                          toast("Error processing response")
                      }




              }
              is NetworkResult.Loading->{
                  binding.progressBar.isVisible = true
//                  val handler = Handler(Looper.getMainLooper())
//                  handler.post {
//                      showProgressbar()
//                  }
              }
          }
      })
    }

    fun clearApplicationCache(context: Context) {
        try {
            val cacheDir: File? = context.cacheDir
            cacheDir?.let { deleteDir(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children: Array<String> = dir.list() ?: return false
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
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
                           // Log.d("text",text)
                            binding.autoCompleteTextView.text = text.removeRange(0,6)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                         txt = text.removeRange(0,6)
                       // binding.autoCompleteTextView.text = txt
                       if (text.isNotEmpty()) {
                               progressbar(true)
                               getDispatchMVC(text.removeRange(0, 6))
                        } else{
                            startBarcodeScan()
                        }
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

            Log.d("rfidNo", data)

            if(!isScannedRfidNumber.contains(data)) {
                isScannedRfidNumber.add(data)
                processRfidNumber(data)
            }
        }
        // Initialize a counter variable
        var updatedRfidCount = 0






//        rfidListTest.forEach { rfidNo ->
//            // Check if the RFID number is not already matched
//            if (!matchedRfidSet.contains(rfidNo)) {
//                // Process the RFID number and check if it was successfully processed
//                if (processRfidNumber(rfidNo)) {
//                    matchedRfidSet.add(rfidNo)
//                }
//            }
//        }

//        rfidListTest.forEach { rfidNo ->
//            // Check if the RFID number is not already processed
//            if (!tempList.contains(rfidNo)) {
//                tempList.add(rfidNo)
//
//                val matchingRFIDs = rfidListOfObject.flatMap { it.rfidNumber }
//                    .filter { it.rfidNumber == rfidNo && it.status != "2" }
//
//                // Reset matching count for each group
//                var matchingCountInGroup = 0
//
//                if (matchingRFIDs.isNotEmpty()) {
//                    // Update the status for all matching RFID numbers to "2"
//                    matchingRFIDs.forEach { matchingRFID ->
//                        matchingRFID.status = "2"
//                        matchingCountInGroup++  // Increment the counter for each updated RFID number in the group
//                        updatedRfidCount++
//                        binding.noReadTags.text = updatedRfidCount.toString()
//                        // Access the corresponding TempItem properties
//                        val tempItem = rfidListOfObject.find { temp -> temp.rfidNumber.any { it == matchingRFID } }
//                        tempItem?.let {
//                            Log.d("Debug", "Status Update: ${matchingRFID.rfidNumber},${tempItem.pkgGroupName},${tempItem.silNo},${matchingRFID.status}")
//                        }
//                    }
//                }
//
//                // Log the count of matching RFID numbers along with the group name outside the loop
//                if (matchingCountInGroup > 0) {
//                    // Access the pkgGroupName from the first TempItem associated with the matchingRFIDs
//                    val tempItemWithPkgGroupName = rfidListOfObject.find { temp ->
//                        matchingRFIDs.any { matchingRFID -> temp.rfidNumber.contains(matchingRFID) }
//                    }
//
//                    Log.d("Debug", "Matching RFID Count in Group ${tempItemWithPkgGroupName?.pkgGroupName}: $matchingCountInGroup")
//                }
//
//                // Iterate through TempDispatch objects to check if all RFID numbers are found
//                for (tempItem in rfidListOfObject) {
//                    val foundRfidNumbers = tempItem.rfidNumber.count { it.status == "2" }
//
//                    if (foundRfidNumbers == tempItem.ctn) {
//                        Log.d("Debug", "All RFID Numbers Found for Group: ${tempItem.pkgGroupName}")
//
//                        runOnUiThread {
//                            dispachItemDetailsAdapter.SilNoFromActivity = tempItem.silNo
//                            dispachItemDetailsAdapter.partNoFromActivity = tempItem.ctn
//                            dispachItemDetailsAdapter.groupNameFromActivity = tempItem.pkgGroupName
//                            Log.d("smvkm", tempItem.pkgGroupName)
//                            dispachItemDetailsAdapter.notifyDataSetChanged()
//                        }
//
//                        // Break the loop if all RFID numbers are found in a group
//                        break
//                    }
//                }
//            }
//        }





//            rfidListTest.forEach {
//                rfidNo = it
//            }


//            scannedRFId.add(RFIDNo(data))


//            if (rfidListOfObject != null && rfidNo.isNotBlank()) {
//                val currentRFID = RFIDNo(rfidNo)
//
//                for (item in rfidListOfObject) {
//                    if (!scannedRFId.contains(currentRFID) && !invalidRfidList.contains(rfidNo)) {
//                        //scannedRFId.add(currentRFID)
//                        Log.d("RFID Scan", "Added to scannedRFId: $rfidNo")
//                    } else {
//                        val res = item.rfidNumber.find { it.rfidNumber == rfidNo && it.statusCode != 2 }
//                        if (res != null) {
//                            res.statusCode = 1
//                            scannedRFId.add(currentRFID)
//                            Log.d("RFID Scan", "Updated status and added to scannedRFId: $rfidNo")
//
//                            if (item.rfidNumber.count { it.statusCode == 1 } == item.ctn) {
//                                Log.d("items", "${item.rfidNumber},${item.pkgGroupName},${item.silNo}")
//                            }
//                        }
//                    }
//                }
//            }
//            rfidListTest.forEach {
//                rfidNo = it

//            if (!tempList.contains(rfidNo)) {
//                tempList.add(rfidNo)
//
//
//                val matchingRFID =
//                    rfidListOfObject.flatMap { it.rfidNumber }.find { it.rfidNumber == rfidNo }
////                Log.d("matchingRFID", matchingRFID.toString())
//                if (matchingRFID == null && !invalidRfidList.contains(rfidNo)) {
//                    invalidRfidList.add(rfidNo)
//                    binding.noInvalidTags.text = invalidRfidList.size.toString()
//                }
//
//                matchingRFID?.let {
//                    val rfidNumber = it.rfidNumber
//                    if (rfidNumber == rfidNo) {
//                        if (!matchedList.contains(rfidNumber)) {
//                            matchedList.add(rfidNumber)
//                            val matcheList = matchedList.distinct()
//                            if (binding.noReadTags.text.toString().toInt() != binding.tvTotalTagList.text.toString().toInt()) {
//                                binding.noReadTags.text = matcheList.size.toString()
//                            }
//                            dispachItemDetailsAdapter.scannedRfidTags.add(rfidNo).toString()
//                            dispachItemDetailsAdapter.notifyDataSetChanged()
//
//                        }
//                    }
//                }
//
//
//                //}
//
//
//                // updating the value for passing the value to viewAllRfid Screen
//                DataStorage.dataList.forEach { rfidTag ->
//                    if (rfidTag.rfidTagNo == rfidNo) {
//                        // Update the status value (You can set the new status value here)
//                        rfidTag.status = "2"
//                        val sortedList = DataStorage.dataList.sortedBy { it.status }
//                        sortedList.filter { it.status == "2" }
////
//                        return@forEach
//                    }
//                }
//
//
//                // updating the status for submit list
////            val iterator = rfidListOfObject.iterator()
////            while (iterator.hasNext()) {
////                val item = iterator.next()
////                // Update the status field in rfidNumber directly
////                try {
////
////                        item.rfidNumber.forEach { rfid ->
////                            if (rfid.rfidNumber == data) {
////                                rfid.status = "2"
////                            }
////                        }
////                }catch (e:Exception){
////                    e.printStackTrace()
////                }
////            }
//
//
//
//
//
////                    if (!rfidList.contains(rfidNo)) {
////                        rfidList.add(rfidNo)
//                        for (tempItem in rfidListOfObject) {
//                            val rfidNumberList = tempItem.rfidNumber.map { it.rfidNumber }
//                            val matchedRfidNumbers = matchedList.intersect(rfidNumberList.toSet())
//
//
//
//                            for (rfidNumber in tempItem.rfidNumber) {
//                                if (matchedRfidNumbers.contains(rfidNumber.rfidNumber) && rfidNumber.rfidNumber == rfidNo && rfidNumber.status.count() <= tempItem.ctn) {
//                                    // Update the status for the matching RFID number
//                                    rfidNumber.status = "2"
//                                    Log.d("gggg",tempItem.pkgGroupName)
//                                    Log.d("Debug", "Matched RFID Numbers: ${matchedRfidNumbers.size}")
//                                    Log.d("statusUpdateMatched", "${rfidNumber.rfidNumber},${tempItem.pkgGroupName},${tempItem.silNo},${rfidNumber.status},${rfidNumber.status.count()}")
//
//
//                                }
//                                Log.d("statusUpdate", "${rfidNumber.rfidNumber},${tempItem.pkgGroupName},${tempItem.silNo},${rfidNumber.status},${rfidNumber.status.count()}")
//
//
//                                tempItem.found = matchedRfidNumbers.size
//                                tempItem.notFound = tempItem.totalCtn - tempItem.found
//                                Log.d("groupName", tempItem.pkgGroupName)
//                                Log.d("found", tempItem.found.toString())
//                                Log.d("tempItem.ctn", tempItem.ctn.toString())
//
//
//                            }
//
//                            if (tempItem.found == tempItem.ctn) {
//                                Log.d("tempItem.found", tempItem.found.toString())
//                                Log.d("tempItem.ctn", tempItem.ctn.toString())
//
//                                runOnUiThread {
//                                    dispachItemDetailsAdapter.SilNoFromActivity = tempItem.silNo
//                                    dispachItemDetailsAdapter.partNoFromActivity = tempItem.ctn
//                                    dispachItemDetailsAdapter.groupNameFromActivity =
//                                        tempItem.pkgGroupName
//                                    Log.d("smvkm", tempItem.pkgGroupName)
//                                    dispachItemDetailsAdapter.notifyDataSetChanged()
//                                }
//
//                                break
//                            }
//
//
//                        }
//                   // }
//
//
//            }
      //  }
    //}
    }




    override fun onRfidListener(item: BinDispatchDetails.BinDispatchDetailsItem,root:ConstraintLayout) {


        item.rfidNumber.forEach {
            Log.d("clickedItem", it.rfidTagNo.toString())
        }

toast(item.groupName.toString())


        }








    /**
     * Execute the loading action
     */
    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                stopBarcodeScan()
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
                        startBarcodeScan()
//                       binding.btnClear.isEnabled = true
//                        binding.btnClear.setTextColor(R.color.white)
                    } catch (e: Exception) {
                        super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
                        e.printStackTrace()
                    }
                }
                binding.apply {
                    btnClear.isEnabled = true
                    //btnClear.alpha = 0.2F
                    btnClear.setTextColor(getColor(R.color.text_default))
                }

            }
        }

        // Set the next reading action
        // Switch the previous reading action to STOP is it was STARTed, and to START if it was STOPped.
        nextReadAction = if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

        // For the buttons, set the name of the action to be executed next
       // val readToggle = findViewById<View>(R.id.button_read_toggle) as Button
        binding.btnStartReading.text = nextReadAction.toResourceString(resources)
    }


    private fun bindObserverToCreateRfidTagStatus(){
        dispatchViewModel.confirmDispatchLiveData.observe(this, Observer {
            hideProgressbar()
            when(it){
             is NetworkResult.Success->{
                 dialogTag.dismiss()
//                 getAllDataFromApi.clear()
//                 dispachItemDetailsAdapter.clearItems()
//                 dispachItemDetailsAdapter.refreshAdapter()
                 if (it.data?.isEmpty()==true){
                     toast("data not processed")
                 } else
                     toast(it.data.toString())

                 rfidListfromAPi.clear()
                 rfidListOfObject.clear()
                 getAllDataFromApi.clear()
                 silNo.clear()
                 dispachItemDetailsAdapter.clearItems()
                 dispachItemDetailsAdapter.refreshAdapter()
                 rfidTagNoFromApiList.clear()
                 binding.autoCompleteTextView.text=""
                 binding.noInvalidTags.text = ""
                 binding.noReadTags.text=""
                 binding.tvCustName.text = ""
                 binding.tvLocation.text = ""
                 binding.tvTotalTagListTwo.text = ""
                 binding.tvTotalTagList.text = ""
                 temp.clear()
                 binding.tvSlash.text = ""
                 dispachItemDetailsAdapter.notifyDataSetChanged()

//                 rfidListfromAPi.clear()
//                 rfidListOfObject.clear()
//                 rfidTagNoFromApiList.clear()
//                 binding.autoCompleteTextView.text=""
//                 binding.noInvalidTags.text = ""
//                 binding.noReadTags.text=""
//                 binding.tvCustName.text = ""
//                 binding.tvLocation.text = ""
//                 binding.tvTotalTagListTwo.text = ""
//                 binding.tvTotalTagList.text = ""
//                 temp.clear()
//                 dispachItemDetailsAdapter.notifyDataSetChanged()

                 nextReadAction =
                     if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

                 // For the buttons, set the name of the action to be executed next
                 // val readToggle = findViewById<View>(R.id.button_read_toggle) as Button
                 binding.btnStartReading.text = nextReadAction.toResourceString(resources)

                 ReadAction.STOP
                     if (scannerConnectedOnCreate) {
                         try {
                             super.getCommScanner()!!.rfidScanner.close()
                         } catch (e: Exception) {
                             super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
                             e.printStackTrace()

                     }
                 }

//                 val intent = Intent(this,MainActivity::class.java)
//                 startActivity(intent)
//                 finish()

             }

                is NetworkResult.Error->{
                    toast(it.message.toString())
                }

                is NetworkResult.Loading->{
                    showProgressbar()
                } } })
    }




   private fun showProgressbar() {
        try {
            if (!isFinishing) {
                runOnUiThread(Runnable {
                    progressDialog.setMessage(Cons.loaderMessage)
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressbar() {
        try {
            if (progressDialog.isShowing && !isFinishing) {
                runOnUiThread(Runnable {
                    progressDialog.hide()
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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



    @SuppressLint("SetTextI18n")
    private fun dialogForTag() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.layout_for_confirmation)
        dialogTag.setCancelable(true)
        dialogTag.show()


        val cancel:MaterialButton = dialogTag.findViewById(R.id.btn_cancel)

        cancel.setOnClickListener {
            dialogTag.dismiss()

        }

        val yes: MaterialButton = dialogTag.findViewById(R.id.bt_yes)
        yes.setOnClickListener {
            if(rfidListOfObject.isNotEmpty()) {
                dialogTag.dismiss()
                showProgressbar()
                var gson = Gson()
                var jsonString = gson.toJson(rfidListOfObject)

                Log.d("RequestBody",jsonString)
                dispatchViewModel.confirmDispatch(rfidListOfObject)
                bindObserverToCreateRfidTagStatus()
            } else{
                toast("No item scanned")
            }

           // disPatchItem(rfidListOfObject)



        }
    }

//     fun provideOkHttpClient(interceptor: RequestBodyInterceptor): OkHttpClient {
//        return OkHttpClient.Builder()
//            .addInterceptor(interceptor)
//            .build()
//    }
//
//     fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("http://10.122.72.129:1001/api/bin-dispatch/")
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//     val retrofit: Retrofit = provideRetrofit(provideOkHttpClient(RequestBodyInterceptor(this)))
//
//    private fun disPatchItem(rfidListOfObject:ArrayList<TempDispatch.TempItem>){
//        val apiService = retrofit.create(RetrofitApi::class.java)
//
//        // Make the API POST request
//        apiService.updateVehicleInfo(rfidListOfObject).enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
//                if (response.code()==200){
//                    hideProgressbar()
//
//                    Toast.makeText(this@DispatchActivity,response.toString(),Toast.LENGTH_LONG).show()
//                } else if(response.code()==400){
//                    Toast.makeText(this@DispatchActivity,"Something went wrong",Toast.LENGTH_LONG).show()
//                } else if (response.code()==404){
//                    Toast.makeText(this@DispatchActivity,response.body(),Toast.LENGTH_LONG).show()
////                    Log.d("response.body()",response.body().toString())
////                    Log.d("response.body()",response.errorBody().toString())
////                    Log.d("response.body()",response.message().toString())
//                }
//
//                else{
//                    Toast.makeText(this@DispatchActivity,response.errorBody().toString(),Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                hideProgressbar()
//                Toast.makeText(this@DispatchActivity,t.localizedMessage, Toast.LENGTH_LONG).show()
//                Log.d("localizedMessage",t.localizedMessage.toString())
//            }
//
//        })
//    }





    private fun dialogForNoItem() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.layout_for_dispatch_alert)
        dialogTag.setCancelable(true)
        dialogTag.show()

        val cancel:MaterialButton = dialogTag.findViewById(R.id.btn_cancel)

        cancel.setOnClickListener {
            dialogTag.dismiss()

        }

        val yes: MaterialButton = dialogTag.findViewById(R.id.bt_yes)
        yes.setOnClickListener {

//            dispatchViewModel.confirmDispatch((rfidListOfObject) as ArrayList<RfidTag>)
//            bindObserverToCreateRfidTagStatus()

        }
    }

    fun logToFile(context: Context, data: String, filename: String) {
        try {
            // Check if external storage is available for writing
            if (isExternalStorageWritable()) {
                // Get the external storage directory for your app
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

                // Create a file object for the specified filename
                val file = File(storageDir, filename)

                // Create a FileWriter to write to the file
                val fileWriter = FileWriter(file, true) // Set 'true' to append data to the file

                // Write the data to the file
                fileWriter.write(data)
                fileWriter.write("\n") // Add a new line after each entry (optional)

                // Close the file writer
                fileWriter.close()

                // Log success message
                println("Data logged to $filename successfully.")
            } else {
                println("External storage is not writable.")
            }
        } catch (e: IOException) {
            // Log any errors that occurred
            e.printStackTrace()
        }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }


    fun viewRfidStatus(){
        val dialogTag = BottomSheetDialog(this)

        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
       dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view=layoutInflater.inflate(R.layout.rfid_status_layout,null)
        dialogTag.setCancelable(true)

        dialogTag.setContentView(view)
        dialogTag.show()
//        dialogTag = Dialog(this)
//        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialogTag.setContentView(R.layout.rfid_status_layout)
//        dialogTag.setCancelable(true)
//        dialogTag.show()


//        localDataBaseViewModel.allData.observe(this) { allData ->
//            allData.forEach {
//                dataList.add(RfidTag(0,it.rfidTagNo, it.status))
//            }
//        }
//
//        localDataBaseViewModel.fetchData()

        val rfidTagList = view.findViewById<RecyclerView>(R.id.rfid_tag_list)
        val totalCount = view.findViewById<TextView>(R.id.tv_count)


        val list = DataStorage.dataList.distinct()
         // list.sortedByDescending { it.status }
        if (list.isEmpty()){
            binding.tvNoDataFound.isVisible= true
        } else {
             totalCount.text = "Total ${list.size}"
            getAllScannedRfidStatusAdapter = AllScannedRfidStatusAdapter()
            //list.sortedByDescending { it.status }
            getAllScannedRfidStatusAdapter.setItems(list)
            rfidTagList.adapter = getAllScannedRfidStatusAdapter
            binding.tvNoDataFound.isVisible= false
        }

        val tvClose  = view.findViewById<TextView>(R.id.tv_close)

        tvClose.setOnClickListener {
            dialogTag.dismiss()
        }

    }


fun progressbar(boolean: Boolean){
    runOnUiThread(Runnable {  binding.progressBar.isVisible = boolean; })

}

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getDispatchMVC(SilNo:String){
    //    val processedGroupSet = mutableSetOf<Pair<String, String>>()


        RetrofitClient.getResponseFromApi().getDispatchMVC(SilNo).enqueue(object:
            Callback<BinDispatchDetails> {
            @SuppressLint("SuspiciousIndentation")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<BinDispatchDetails>, response: retrofit2.Response<BinDispatchDetails>) {

                if (response.isSuccessful) {
                        tempRFidList.clear()
                    val dataListSize = dataList.distinct().size

                    binding.autoCompleteTextView.text = ""
                    txt = ""
                    val result = 20000 - dataList.distinct().size

                    if (response.body()?.isEmpty() == true){
                        if (!isFinishing && !isDestroyed) {
                            Snackbar.make(binding.root, "No Data Found", Snackbar.LENGTH_SHORT).show()
                            binding.autoCompleteTextView.text = ""
                        }

//                      binding.tvNoDataFound.isVisible = true
                        binding.btnClear.alpha = 0.3F
                        startBarcodeScan()
                    }else if (dataListSize<=20000) {
                        //Log.d("responsbodysize", response.body()?.size.toString())
                        if (response.body()?.size!! >1) {
                            response.body()?.forEach {
                                ifSingleSilHaveMoretheOneData.add(it.ctn.toString())
                            }
                        }
                        val tempRFidSet = HashSet<String>()
                        response.body()?.forEach {
                            it.rfidNumber.forEach {
                                tempRFidSet.add(it.rfidTagNo.toString())
                            }
                        }
                        tempRFidList = tempRFidSet.toList() as ArrayList<String>
                            if (tempRFidList.size < result) {
                                try {

                                    //val uniqueRfidNumbers = HashSet<String>()
                                    val partNo: List<String>? = response.body()?.map { it.partNo }
                                    response.body()?.get(0)?.rfidNumber?.forEach { i ->
                                        println("Processing element: $i")
                                        val rfidTagNo = i.rfidTagNo.toString()
                                        //if (!uniqueRfidNumbers.contains(rfidTagNo)) {
                                        // uniqueRfidNumbers.add(rfidTagNo)
                                        //Log.d("Duplicate found:", rfidTagNo)
                                        DataStorage.dataList.add(
                                            RfidTag(
                                                rfidTagNo,
                                                partNo?.joinToString(", ") ?: "",
                                                ""
                                            )
                                        )
                                        //}

                                    }

                                    for (currentStatus in response.body()?.get(1)?.rfidNumber!!) {
                                        status = currentStatus.status.toString()
                                        statusList.add(RfidDispatchedStatusList(currentStatus.status.toString()))
                                    }

                                } catch (e: Exception) {

                                }




                                try {
                                    response.body()?.forEach {

                                        val result = 20000 - dataListSize
                                        Log.d("result", result.toString())
                                        if (dataListSize <= 20000) {

                                            getAllDataFromApi.add(
                                                BinDispatchDetails.BinDispatchDetailsItem(
                                                    it.ctn,
                                                    it.customerAddress,
                                                    it.customerCode,
                                                    it.customerName,
                                                    it.groupName,
                                                    it.lotSize,
                                                    it.partName,
                                                    it.partNo,
                                                    "it.pkgPartNo",
                                                    it.rfidNumber,
                                                    "it.shipQty",
                                                    it.weight,
                                                    it.silNo,
                                                    false,
                                                    0,
                                                    0,
                                                    0,
                                                    0

                                                )
                                            )


                                            val ctnList = ArrayList<Int>()
                                            val distinctGroupNames = HashSet<String>()

                                            for (obj in getAllDataFromApi) {
                                                if (obj.partNo !in distinctGroupNames || obj.silNo !in distinctGroupNames) {
                                                    ctnList.add(obj.ctn)
                                                    distinctGroupNames.add(obj.partNo)
                                                    distinctGroupNames.add(obj.silNo)
                                                }
                                            }


                                            val totalCtn = ctnList.sum()
                                            getCtn =
                                                totalCtn // Update getCtn with the totalCtn value

                                            // binding.tvTotalTagList.text = "/$getCtn"
                                            binding.tvTotalTagList.text = getCtn.toString()
                                            binding.tvTotalTagListTwo.text = getCtn.toString()
//                            Log.d("totalallala", dispachItemDetailsAdapter.totalCtn.toString())

                                            dispachItemDetailsAdapter =
                                                DispatchedItemDetailsAdapter(
                                                    this@DispatchActivity,
                                                    getAllDataFromApi as ArrayList<BinDispatchDetails.BinDispatchDetailsItem>
                                                )
                                            binding.listOfItem.adapter = dispachItemDetailsAdapter
                                            dispachItemDetailsAdapter.refreshAdapter()
//                                Log.d("getAllDataFromApi", getAllDataFromApi.toString())

                                        } else {
                                            Snackbar.make(
                                                binding.root,
                                                "Please scan in next batch",
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()

                                }


try {
    runOnUiThread(Runnable {
        binding.tvNoDataFound.isVisible = false
        binding.tvCustName.text =
        response.body()?.get(0)?.customerName.toString()
        binding.tvLocation.text = response.body()?.get(0)?.customerAddress })

}catch (e:Exception){
    e.printStackTrace()
}

                                try {

                                    dispatchDetails(response)

                                }catch (e:Exception)
                                {
                                    e.printStackTrace()
                                }




                                // stopBarcodeScan()
                              /*  binding.btnClear.alpha = 10F
                                binding.btnClear.setTextColor(getColor(R.color.white))*/


                                /*
                            response.body()?.forEach {
                                for (rfid in it.rfidNumber)
                                    groupwiseList.add(
                                        GroupWiseDataModel(
                                            it.groupName,
                                            it.ctn.toString(),
                                            rfid.rfidTagNo.toString(),
                                            rfid.status.toString()
                                        )
                                    )

                              //  AllGroupName.add(AllGroupNameModel(it.groupName, it.ctn))


                            }*/

                                //  pkgGroupName = response.body()?.get(0)?.groupName.toString()
                                //   customerName = response.body()?.get(0)?.customerName.toString()

                                //   partName = response.body()?.get(0)?.partName.toString()

                        //

                          } else{
                              Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                          }

                    } else if (dataListSize>20000){
                        binding.progressBar.isVisible = false
                        Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                    } else{
                        binding.progressBar.isVisible = false
                        Snackbar.make(binding.root,"Please scan in next batch",Snackbar.LENGTH_SHORT).show()
                    }

                } else if (response.body().toString()=="There is no Related Data For This Silnumber"){
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""

                    toast("There is no Related Data For This Silnumber")
                } else if (response.code()==400){
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""
                    toast(response.message())
                } else if (response.code()==500){
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""
                    toast(response.message())
                } else if (response.code()==404){
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""
                    toast(response.message())
                }
                // handle  Api error
                progressbar(false)

            }

            override fun onFailure(call: Call<BinDispatchDetails>, t: Throwable) {
                progressbar(false)
                if (t.localizedMessage == "Expected BEGIN_ARRAY but was STRING at line 1 column 1 path \$") {
                    Snackbar.make(binding.root, "There is no Related Data For This Silnumber", Snackbar.LENGTH_LONG).show()
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""
                } else {
                    toast("Something went wrong")
                    binding.progressBar.isVisible = false
                    binding.autoCompleteTextView.text = ""

                }

            }
        })
    }



    fun writeToFileExternal(context: Context, fileName: String, data: String) {
        try {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED != state) {
                Log.d("writeToFileExternal", "External storage is not available")
                return
            }

            val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (externalDir == null) {
                Log.d("writeToFileExternal", "External directory is null")
                return
            }

            val file = File(externalDir, fileName)

            val fileOutputStream = FileOutputStream(file, true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)

            // Append a newline character before adding new content
            if (file.length() > 0) {
                outputStreamWriter.append('\n')
            }

            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }





    private fun processRfidNumber(rfidNo: String): Boolean {
        val processedGroupSet = mutableSetOf<Pair<String, String>>()
        // Check if the RFID number is not already matched
        if (!matchedRfidSet.contains(rfidNo)) {
             //matchedRfidSet.add(rfidNo)
            val matchingRFIDs = rfidListOfObject.flatMap { it.rfidNumber }
                .filter { rfid ->
                    rfid.rfidNumber == rfidNo && rfid.status != "2"
                }

            // Reset matching count for each group


            // Flag to track if a group is already processed
            var isGroupProcessed = false

            if (matchingRFIDs.isNotEmpty()) {
                matchingRFIDs.forEach { matchingRFID ->
                    if (!scannedRfid.contains(matchingRFID.rfidNumber)){
                        scannedRfid.add(matchingRFID.rfidNumber)
                    val tempItem = rfidListOfObject.find { temp -> temp.rfidNumber.any { it == matchingRFID } }
                    tempItem?.let {
                       // tempItem.notFound = it.ctn
                        // Check if the RFID number belongs to a different group
                        Log.d("NotFound", tempItem.notFound.toString())
                        val groupKey = Pair(tempItem.silNo, tempItem.pkgGroupName.toString())
                        val isSameGroup = processedGroupSet.contains(groupKey)

                        val foundRfidNumbers = tempItem.rfidNumber.count { it.status == "2"  }
                        if ( (foundRfidNumbers < tempItem.ctn) && !isSameGroup) {
                            matchingRFID.status = "2"
                            matchingCountInGroup++
                            foundRfidList.add(matchingRFID.rfidNumber)

                             //updating the value for passing the value to viewAllRfid Screen
                          DataStorage.dataList.forEach { rfidTag ->
                         if (rfidTag.rfidTagNo == matchingRFID.rfidNumber) {
                        // Update the status value (You can set the new status value here)
                        rfidTag.status = "2"
                        val sortedList = DataStorage.dataList.sortedBy { it.status }
                        sortedList.filter { it.status == "2" }

                        return@forEach
                    }
                }


                            val foundRfidNumbers = tempItem.rfidNumber.count { it.status == "2"  }
//                            if (!matchRFidListInGroupCount.contains(matchingRFID.rfidNumber)) {
                                //matchRFidListInGroupCount.add(matchingRFID.rfidNumber)
                                tempItem.found = foundRfidNumbers
                                tempItem.notFound = (tempItem.ctn - foundRfidNumbers)
//                                Log.d("groupName", "${tempItem.pkgGroupName},${tempItem.found},${tempItem.notFound}")
//                                Log.d("found", tempItem.found.toString())
//                                Log.d("tempItem.ctn", tempItem.ctn.toString())
//                                Log.d("matcher",matchingRFID.rfidNumber)
//                                Log.d("Details", "${matchingRFID.rfidNumber},${matchingRFID.status},${tempItem.ctn},${tempItem.pkgGroupName},${matchRFidListInGroupCount.distinct().size},${tempItem.found},${tempItem.notFound}")

                            //}
                            binding.noReadTags.text = foundRfidList.distinct().size.toString()


                            // Access the corresponding TempItem properties
                            //Log.d("Debug", "Status Update: ${matchingRFID.rfidNumber},${tempItem.pkgGroupName},${tempItem.silNo},${matchingRFID.status.length}")

                            // Check if all RFID numbers are found for the current group
//                            val foundRfidNumbers = tempItem.rfidNumber.count { it.status == "2"  }
                            if (foundRfidNumbers == tempItem.ctn && !isSameGroup) {
                                processedGroupSet.add(groupKey)
                                matchedCountMap[groupKey] = 0

                                matchRFidListInGroupCount.clear()
                                index = rfidListOfObject.indexOf(tempItem)
                                isGroupProcessed = true // Set the flag to true if all RFID numbers are found in the group

                               // Log.d("Debug", "All RFID Numbers Found for Group: ${tempItem.pkgGroupName},${tempItem.ctn}")

                                runOnUiThread {
//                                    dispachItemDetailsAdapter.SilNoFromActivity = tempItem.silNo
//                                    dispachItemDetailsAdapter.partNoFromActivity = tempItem.ctn
//                                    dispachItemDetailsAdapter.groupNameFromActivity = tempItem.pkgGroupName
//                                    dispachItemDetailsAdapter.positionFromActivity = index
                                    //Log.d("smvkm", tempItem.pkgGroupName)
                                    dispachItemDetailsAdapter.highlightedPositions.add(tempItem.silNo)
                                    dispachItemDetailsAdapter.highlightedPositions.add(tempItem.pkgGroupName.toString())
                                    dispachItemDetailsAdapter.notifyDataSetChanged()

                                }
                            }
                        } else {
                            //Log.d("Condition", "true")
                            if (!inRfidList.contains(rfidNo) ) {
                                inRfidList.add(rfidNo)
                                binding.noInvalidTags.text = inRfidList.distinct().size.toString()
                            }
                        }
                    }

                }
                }
            } else if(!inRfidList.contains(rfidNo)  ) {
                inRfidList.add(rfidNo)
                binding.noInvalidTags.text = inRfidList.distinct().size.toString()

            }

            // Log the count of matching RFID numbers along with the group name outside the loop
            if (matchingCountInGroup > 0) {
                // Access the pkgGroupName from the first TempItem associated with the matchingRFIDs
                val tempItemWithPkgGroupName = rfidListOfObject.find { temp ->
                    matchingRFIDs.any { matchingRFID -> temp.rfidNumber.contains(matchingRFID) }
                }

                Log.d("Debug", "Matching RFID Count in Group ${tempItemWithPkgGroupName?.pkgGroupName}: $matchingCountInGroup")
            }

            // Update the set to mark the RFID number as matched
            return true
        }


        // Return false if the RFID number was not successfully processed (already matched)
        return false
    }

    override fun onPostResume() {
        super.onPostResume()
        startBarcodeScan()
    }

fun toast(message:String){
    try {
       /* runOnUiThread(Runnable {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        })*/
    } catch (e:Exception){
        e.printStackTrace()
    }

}
    fun dispatchDetails(response:retrofit2.Response<BinDispatchDetails>){
        val storedValue = sharePref.getData(Cons.userId)
        val responseBody = response.body()
        responseBody?.forEach {
            val groupName = it.groupName
            val silNo = it.silNo
            val groupKey = Pair(silNo, groupName)
            val isSameGroup = processedGroupSetForRfidListOfObject.contains(groupKey)
            if (isSameGroup){
              toast("SIL already Scanned")
            } else  {
                val tempItem = TempDispatch.TempItem(
                    it.ctn,
                    storedValue.toString(),
                    it.customerCode,
                    it.found,
                    (it.ctn - it.found),
                    it.partNo,
                    it.groupName,
                    it.rfidNumber.filter { rfid -> rfid.statusCode != 2 }.map { rfid ->
                        TempDispatch.TempItem.RfidNumber(
                            rfid.rfidTagNo.toString(),
                            rfid.status.toString()
                        )
                    },
                    it.silNo
                )
                rfidListOfObject.add(tempItem)
                processedGroupSetForRfidListOfObject.add(groupKey)
                uniqueCtnSet.add(silNo)
                uniqueCtnSet.add(groupName)
            }
        }
    }











}




