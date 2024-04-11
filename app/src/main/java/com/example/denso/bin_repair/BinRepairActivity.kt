package com.example.denso.bin_repair

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.bin_repair.adapter.RepairAdapter
import com.example.denso.bin_repair.bin_view_model.BinRepairViewModel
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.bin_repair.model.OutResponseFromApi
import com.example.denso.bin_repair.model.RepairStatus
import com.example.denso.bin_stock_take.adapter.TagRecyclerViewAdapter
import com.example.denso.databinding.ActivityBinRepairBinding
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import com.example.denso.utils.sharePreference.SharePref
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Until
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BinRepairActivity : BaseActivity(),RFIDDataDelegate {
    lateinit var binding:ActivityBinRepairBinding
    private val binRepairViewModel : BinRepairViewModel by viewModels()

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true

    lateinit var scannedRfidTagNo:ArrayList<BinRepairModel>
     lateinit var repairOutList:ArrayList<BinRepairModel>

    lateinit var rfidTagList:ArrayList<BinRepairModel>

    lateinit var progressDialog: ProgressDialog

    lateinit var repairStatus:ArrayList<RepairStatus>

    lateinit var repairAdapter: RepairAdapter
    private var adapter:TagRecyclerViewAdapter ? = null

    private var isRefreshingShowRange = false

    lateinit var tempList:ArrayList<String>
    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinRepairBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        progressDialog = ProgressDialog(this)

        scannedRfidTagNo  = arrayListOf()

        rfidTagList =  arrayListOf()

        repairOutList = arrayListOf()
        repairStatus =  arrayListOf()
        tempList = arrayListOf()

        initRecyclerView()

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

        binding.btnStartReading.setOnClickListener {
            runReadActionTag()
        }

//        binding.imBack.setOnClickListener {
//
//            navigateUp()
//        }


        binding.RadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rd_in -> {
                    if (binding.tvRfidNo.text.isNotEmpty()) {
                        binRepairViewModel.binRepairIn(scannedRfidTagNo)
                        binObserverBinRepair()
                    } else{
                        Toast.makeText(this,"Please Scan Rfid Tag",Toast.LENGTH_SHORT).show()
                    }

                }
                R.id.rd_out -> {
                    if (binding.tvRfidNo.text.isNotEmpty()) {
                        binRepairViewModel.binOutRepair(repairOutList)
                        bindObserverForOutRepairModel()
                    } else{
                        Toast.makeText(this,"Please Scan Rfid Tag",Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }



    }


    private fun bindObserverForOutRepairModel(){
        binRepairViewModel.binOutRepairResponseLiveData.observe(this, Observer {
            progressDialog.hide()
            when(it){
                is NetworkResult.Success->{
                    try {
                        scannedRfidTagNo.clear()
                        binding.tvResponse.text = "Repaired Bin out successfully!"
                        binding.tvRfidNo.text = ""
                        //binding.tvResponse.text = it.data.toString()
                        repairStatus.clear()
                    } catch (e:JsonSyntaxException){
                        binding.tvResponse.text = "Repaired Bin out successfully!"
                    }
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





    private fun binObserverBinRepair(){
        binRepairViewModel.getResponseFromBinRepairIn.observe(this, Observer {
            progressDialog.hide()
            when(it){
                is NetworkResult.Success->{
                    try {
                        repairOutList.clear()
                        binding.tvResponse.text = "Repair request submitted successfully!"
                        binding.tvRfidNo.text = ""
//                    binding.tvResponse.text = it.data.toString().replace("{", " ").replace("}", " ")
//                    val str = it.data.toString().replace("{", " ").replace("}", " ")
//                    Log.d("stsus",it.data.toString().replace("{", " ").replace("}", " "))
//                    Log.d("stsusOne",str.removeRange(0,3))
                        repairStatus.clear()
                    } catch (e:JsonSyntaxException){
                        binding.tvResponse.text = "Repair request submitted successfully!"
                    }

//
//                    for(i in it.data.toString())
//                        Log.d("status", i.toString())
//                        repairStatus.add(RepairStatus(it.data.toString()))
//
//                    repairAdapter = RepairAdapter(repairStatus)
//                    binding.listOfItem.adapter = repairAdapter

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

    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    override fun onRFIDDataReceived(p0: CommScanner?, p1: RFIDDataReceivedEvent) {

//        handler!!.post {
//            readData(p1)
//        }

        handler!!.post {
            readData(p1)
            if (adapter != null) {
                // Reflect the data added to RecycleView.
                adapter!!.notifyDataSetChanged()

                // Since the event is not issued from RecycleView.scrollPosition to OnScroll,
                // Update the display range manually.
                refreshShowRangeIfNeeded()

                // Update TotalTags since the number of tags has been updated.
                refreshTotalTags()

                // Scroll to the lowest position when adding tag.
                if (adapter!!.itemCount > 0) {
                    binding.list0fRfid.scrollToPosition(adapter!!.itemCount - 1)
                }
            }
        }
    }

    private fun readData(rfidDataReceivedEvent: RFIDDataReceivedEvent) {

        for (i in rfidDataReceivedEvent.rfidData.indices) {
            var data = ""
            val uii = rfidDataReceivedEvent.rfidData[i].uii
            for (loop in uii.indices) {
                data += String.format("%02X ", uii[loop]).trim { it <= ' ' }
            }

            binding.tvRfidNo.text = data
            adapter?.addTag(data)
            if (!tempList.contains(data)) {
                tempList.add(data)
                scannedRfidTagNo.add(BinRepairModel(data, "1"))
                repairOutList.add(BinRepairModel(data, "0"))
            }

            //scannedRfidTagNo.add("0x$data")



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
     * Execute the loading action
     */
    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                binding.apply {
//                    btnClear.isEnabled = false
//                    btnClear.setTextColor(getColor(R.color.white))
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
//                    btnClear.isEnabled = true
//                    btnClear.alpha = 0.2F
//                    btnClear.setTextColor(getColor(R.color.text_default))
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

    /**
     * Move to the upper level at the time of screen transition
     */
    private  fun navigateUp() {
        // Tag reading end
        if (scannerConnectedOnCreate) {
            // When the tag reading is started
            if (nextReadAction == ReadAction.STOP) {
                // Tag loading end
                runReadActionTag()
            }

            // Remove delegate
            super.getCommScanner()?.rfidScanner?.setDataDelegate(null)
        }
        handler = null

        disposeFlg = false

        // Although there is such embedded navigation function "Up Button" in Android,
        // since it doesn't meet the requirement due to the restriction on UI, transition the the screen using button events.
        val intent = Intent(this@BinRepairActivity, MainActivity::class.java)
        startActivity(intent)

        // Stop the Activity because it becomes unnecessary since the parent Activity is returned to.
        finish()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun initRecyclerView() {


        // Specify this to improve performance since the size of RecyclerView is not changed
        val displayMetrics = DisplayMetrics()
        val params = binding.list0fRfid.layoutParams
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        if (height < 1280) {
            params.height = (335 * Resources.getSystem().displayMetrics.density).toInt()
            binding.list0fRfid.layoutParams = params
            binding.list0fRfid.setHasFixedSize(true)
        }
        binding.list0fRfid.setHasFixedSize(true)

        // Use LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        binding.list0fRfid.layoutManager = layoutManager

        // Specify Adapter
        adapter = TagRecyclerViewAdapter(this, this.windowManager)
        binding.list0fRfid.adapter = adapter

        // Receive the scroll event and the touch event.
        binding.list0fRfid.addOnScrollListener(OnScrollListener())
        binding.list0fRfid.addOnItemTouchListener(OnItemTouchListener())

        // Update the display.
        adapter!!.notifyDataSetChanged()
    }






    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // Do not scroll again when scrolled in accordance with the updated display range
            if (isRefreshingShowRange) {
                return
            }

            // Acquire the current scroll position.
            val currentScrollPosition =
                (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

            // Confirm whether it is necessary to update the display range, otherwise terminate the process.
            if (!adapter!!.needsRefreshShowRange(currentScrollPosition)) {
                return
            }

            // Update the display range from here.
            isRefreshingShowRange = true

            // Update the display range.
            // Since notifyDataSetChanged cannot be called immediately on the same frame with onScrolled, it will be executed later.
            recyclerView.post(RefreshShowRangeAction(currentScrollPosition))
        }

        // The thread to update the display range.
        // Declare your own class since it is required that property has to be in the action posted to RecyclerView
        private inner class RefreshShowRangeAction internal constructor(private val currentScrollPosition: Int) :
            Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                try {
                    // Update the display range and scroll to a new position.
                    val newScrollPosition = adapter!!.refreshShowRangeIfNeeded(currentScrollPosition)
                    adapter!!.notifyDataSetChanged()
                    binding.list0fRfid.scrollToPosition(newScrollPosition)

                    // Finish updating the display range.
                    isRefreshingShowRange = false
                } catch (e: Exception) {
                    Log.d("DEMO_SP1", "Exception " + e.message)
                }
            }
        }
    }

    /**
     * The touch event in RecyclerView is processed
     */
    internal inner class OnItemTouchListener : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            // Do not allow to touch while updating the display range.
            return isRefreshingShowRange
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }


    private fun refreshTotalTags() {
        // Update ‘Total tags’.
        val storedTagCount = adapter!!.storedTagCount
        //binding.noReadTags.text = storedTagCount.toString()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun refreshShowRangeIfNeeded() {
        // Do not scroll again when scrolled in accordance with the updated display range
        if (isRefreshingShowRange) {
            return
        }

        // Acquire the current scroll position.
        val currentScrollPosition = (binding.list0fRfid.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

        // Confirm whether it is necessary to update the display range, otherwise terminate the process.
        if (!adapter!!.needsRefreshShowRange(currentScrollPosition)) {
            return
        }

        // Update the display range from here.
        isRefreshingShowRange = true

        // Update the display range and scroll to a new position.
        val newScrollPosition = adapter!!.refreshShowRangeIfNeeded(currentScrollPosition)
        adapter!!.notifyDataSetChanged()
        binding.list0fRfid.scrollToPosition(newScrollPosition)

        // Finish updating the display range.
        isRefreshingShowRange = false
    }


}