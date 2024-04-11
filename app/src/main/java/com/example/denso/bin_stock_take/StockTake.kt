package com.example.denso.bin_stock_take

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
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDData
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.bin_stock_take.adapter.BinStockTakeResponseFromApiAdapter
import com.example.denso.bin_stock_take.adapter.TagRecyclerViewAdapter

import com.example.denso.bin_stock_take.bin_stock_take_view_model.BinStockTakeViewModel
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import com.example.denso.databinding.ActivityStockTakeBinding
import com.example.denso.dispatch.RFIDNo
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import com.example.denso.utils.sharePreference.SharePref
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StockTake : BaseActivity(),RFIDDataDelegate {
    lateinit var binding:ActivityStockTakeBinding
    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()

    private var isRefreshingShowRange = false

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true

    lateinit var binStockTakeResponseFromApiAdapter: BinStockTakeResponseFromApiAdapter


    private val binStockTakeViewModel:BinStockTakeViewModel by viewModels()

    lateinit var scannedRfidTagNo:ArrayList<String>
    lateinit var list:MutableList<RFIDData>

    lateinit var allRfidTagsno:ArrayList<String>

    // adapter for display scanned Rfid Tag no
    private var adapter: TagRecyclerViewAdapter? = null

    lateinit var progressDialog:ProgressDialog

    lateinit var rfidDataReceivedEvent: RFIDDataReceivedEvent

    lateinit var rfidList:ArrayList<String>

    lateinit var sharePref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockTakeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rfidList = arrayListOf()

        scannedRfidTagNo  =  arrayListOf()
        allRfidTagsno = arrayListOf()
        list = mutableListOf()

        initRecyclerView()
        progressDialog = ProgressDialog(this)


        scannerConnectedOnCreate = super.isCommScanner()

        rfidDataReceivedEvent = RFIDDataReceivedEvent(list)


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




        try {
            if (intent.getBooleanExtra("isCommingFromScrep", true)) {
                rfidList = intent.getSerializableExtra(Cons.RFID) as ArrayList<String>
                binding.btnStartReading.isVisible = false
                binding.tvReadTag.isVisible = false
                binding.noReadTags.isVisible = false
                binding.btnView.isVisible = false
                binding.btnClear.isVisible = false
                binding.tvStockScreen.text = "Scrap View"

                val recyclerView: RecyclerView =
                    binding.listOfStockTake // Reference to your RecyclerView
                val marginTopInPixels = 192 // Set the desired margin top in pixels

                val layoutParams = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    layoutParams.leftMargin,
                    marginTopInPixels,
                    layoutParams.rightMargin,
                    layoutParams.bottomMargin
                )

                recyclerView.layoutParams = layoutParams

                binStockTakeViewModel.binStockTake(rfidList)
                bindObserverToGetBinStockTakeDetails()
            }
        } catch (e:Exception){

        }



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

        binding.apply {
            btnClear.setOnClickListener {
                clearDataDisplay()
            }
            btnView.setOnClickListener {
                //allRfidTagsno = scannedRfidTagNo
                binding.btnStartReading.text = "Start"
                if (scannedRfidTagNo.isNotEmpty()) {
                    binStockTakeViewModel.binStockTake(scannedRfidTagNo)
                    bindObserverToGetBinStockTakeDetails()
                } else{
                    Toast.makeText(this@StockTake,"Please scan tags",Toast.LENGTH_SHORT).show()
                }
            }


            btnClear.setOnClickListener {
                adapter?.clearTags()
                adapter?.notifyDataSetChanged()
                scannedRfidTagNo.clear()
                ll.isVisible  = false
                binding.noReadTags.text = ""
            }

            imBack.setOnClickListener {
                navigateUp()
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
     * Execute the loading action
     */
    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                binding.apply {
//                    btnClear.isEnabled = false
//                    btnClear.setTextColor(getColor(R.color.white))
                    btnView.isEnabled = false
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
                    btnView.isEnabled = true
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

    private fun readData(rfidDataReceivedEvent: RFIDDataReceivedEvent) {

        for (i in rfidDataReceivedEvent.rfidData.indices) {
            var data = ""
            val uii = rfidDataReceivedEvent.rfidData[i].uii
            for (loop in uii.indices) { data += String.format("%02X ", uii[loop]).trim { it <= ' ' }
            }

            if (!scannedRfidTagNo.contains(data)) {
                scannedRfidTagNo.add(data)
                adapter!!.addTag(data)
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
                adapter = null
                //recyclerView = null
                disposeFlg = false
                finish()
                return true
            }
        }
        return false
    }

    /**
     * Initialize the RecyclerView
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        // Specify this to improve performance since the size of RecyclerView is not changed
        val displayMetrics = DisplayMetrics()
        val params = binding.listOfStockTake.layoutParams
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        if (height < 1280) {
            params.height = (335 * Resources.getSystem().displayMetrics.density).toInt()
            binding.listOfStockTake.layoutParams = params
            binding.listOfStockTake.setHasFixedSize(true)
        }
        binding.listOfStockTake.setHasFixedSize(true)

        // Use LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        binding.listOfStockTake.layoutManager = layoutManager

        // Specify Adapter
        adapter = TagRecyclerViewAdapter(this, this.windowManager)
        binding.listOfStockTake.adapter = adapter

        // Receive the scroll event and the touch event.
        binding.listOfStockTake.addOnScrollListener(OnScrollListener())
        binding.listOfStockTake.addOnItemTouchListener(OnItemTouchListener())

        // Update the display.
        adapter!!.notifyDataSetChanged()
    }

    /**
     * The event of the scroll in RecyclerView is processed
     */
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
                    binding.listOfStockTake.scrollToPosition(newScrollPosition)

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


    @SuppressLint("NotifyDataSetChanged")
    override fun onRFIDDataReceived(
        scanner: CommScanner,
        rfidDataReceivedEvent: RFIDDataReceivedEvent
    ) {
        // Control between threads
        handler!!.post {
            readData(rfidDataReceivedEvent)
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
                    binding.listOfStockTake.scrollToPosition(adapter!!.itemCount - 1)
                }
            }
        }
    }


    /**
     * Update TotalTags
     */
    private fun refreshTotalTags() {
        // Update ‘Total tags’.
        val storedTagCount = adapter!!.storedTagCount
        binding.noReadTags.text = storedTagCount.toString()
    }


    /**
     * Update the display range if it is necessary
     * TODO: I want to optimize the implementation of the OnScrollListener class and the part that has been covered
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshShowRangeIfNeeded() {
        // Do not scroll again when scrolled in accordance with the updated display range
        if (isRefreshingShowRange) {
            return
        }

        // Acquire the current scroll position.
        val currentScrollPosition = (binding.listOfStockTake.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

        // Confirm whether it is necessary to update the display range, otherwise terminate the process.
        if (!adapter!!.needsRefreshShowRange(currentScrollPosition)) {
            return
        }

        // Update the display range from here.
        isRefreshingShowRange = true

        // Update the display range and scroll to a new position.
        val newScrollPosition = adapter!!.refreshShowRangeIfNeeded(currentScrollPosition)
        adapter!!.notifyDataSetChanged()
        binding.listOfStockTake.scrollToPosition(newScrollPosition)

        // Finish updating the display range.
        isRefreshingShowRange = false
    }

    /**
     * Implement clear list when display
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun clearDataDisplay() {
        adapter!!.clearTags()
        adapter!!.notifyDataSetChanged()
        refreshTotalTags()
    }


    private fun bindObserverToGetBinStockTakeDetails(){
        binStockTakeViewModel.binStockTakeModelResponseLiveData.observe(this, Observer {
            progressDialog.hide()

            when(it){
                is NetworkResult.Success->{
                    Log.d("Error","Success")
//                    clearDataDisplay()
                      if (it.data?.isEmpty() == true){
                          binding.tvNoDataFound.isVisible=true }
                        binStockTakeResponseFromApiAdapter = BinStockTakeResponseFromApiAdapter(it.data as ArrayList<BinStockResponseFromApiModel.BinStockResponseFromApiModelItem>)
                        binding.listOfStockTake.adapter = binStockTakeResponseFromApiAdapter
                    binding.ll.isVisible = true

                }

                is NetworkResult.Error->{
                    Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                    Log.d("Error","Error")

                }
                is NetworkResult.Loading->{
                    Log.d("Error","Loading")
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
        adapter = null
        disposeFlg = false

        // Although there is such embedded navigation function "Up Button" in Android,
        // since it doesn't meet the requirement due to the restriction on UI, transition the the screen using button events.
        val intent = Intent(application, MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVQ1ITY_CLEAR_TOP)
        startActivity(intent)

        // Stop the Activity because it becomes unnecessary since the parent Activity is returned to.
        finish()
    }


}