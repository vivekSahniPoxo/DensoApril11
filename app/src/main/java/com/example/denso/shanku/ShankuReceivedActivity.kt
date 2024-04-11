package com.example.denso.shanku

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.ui.NavigationUI.navigateUp
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
import com.example.denso.databinding.ActivityShankuReceivedBinding
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import com.example.denso.utils.sharePreference.SharePref
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShankuReceivedActivity : BaseActivity(), RFIDDataDelegate {
    lateinit var binding:ActivityShankuReceivedBinding
    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()
    private var adapter: TagRecyclerViewAdapter? = null
    lateinit var dialogTag:Dialog

    private var isRefreshingShowRange = false

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true
    lateinit var scannedRfidTagNo:ArrayList<String>

    lateinit var binStockTakeResponseFromApiAdapter: BinStockTakeResponseFromApiAdapter

    lateinit var list:MutableList<RFIDData>

    lateinit var allRfidTagsno:ArrayList<String>

    // adapter for display scanned Rfid Tag no


    lateinit var progressDialog:ProgressDialog

    lateinit var rfidDataReceivedEvent: RFIDDataReceivedEvent

    lateinit var rfidList:ArrayList<String>

    private val binStockTakeViewModel: BinStockTakeViewModel by viewModels()
    lateinit var sharePref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShankuReceivedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scannedRfidTagNo = arrayListOf()

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

        rfidList = arrayListOf()

        scannedRfidTagNo  =  arrayListOf()
        allRfidTagsno = arrayListOf()
        list = mutableListOf()
        dialogTag = Dialog(this)
        initRecyclerView()
        progressDialog = ProgressDialog(this)

        binding.btnConfirm.setOnClickListener {
           // Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
//            binStockTakeViewModel.confirmReceiving(rfidList)
//            bindObserverToGetBinStockTakeDetails()
           dialogForTag()
        }


        scannerConnectedOnCreate = super.isCommScanner()

        rfidDataReceivedEvent = RFIDDataReceivedEvent(list)







//
//        try {
//            if (intent.getBooleanExtra("isCommingFromScrep", true)) {
//                rfidList = intent.getSerializableExtra(Cons.RFID) as ArrayList<String>
//                binding.btnStartReading.isVisible = false
//                binding.tvReadTag.isVisible = false
//                binding.noReadTags.isVisible = false
//                binding.btnSubmit.isVisible = false
//                binding.btnClear.isVisible = false
//                binding.tvStockScreen.text = "Scrap View"
//
//                val recyclerView: RecyclerView =
//                    binding.listOfStockTake // Reference to your RecyclerView
//                val marginTopInPixels = 192 // Set the desired margin top in pixels
//
//                val layoutParams = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
//                layoutParams.setMargins(
//                    layoutParams.leftMargin,
//                    marginTopInPixels,
//                    layoutParams.rightMargin,
//                    layoutParams.bottomMargin
//                )
//
//                recyclerView.layoutParams = layoutParams
//
//                binStockTakeViewModel.confirmReceiving(rfidList)
//                bindObserverToGetBinStockTakeDetails()
//            }
//        } catch (e:Exception){
//
//        }



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
            btnSubmit.setOnClickListener {
                //allRfidTagsno = scannedRfidTagNo
                binding.btnStartReading.text = "Start"
                if (scannedRfidTagNo.isNotEmpty()) {
                    binStockTakeViewModel.binStockTake(scannedRfidTagNo)
                   // bindObserverToGetBinStockTakeDetails()
                } else{
                    Toast.makeText(this@ShankuReceivedActivity,"Please scan tags", Toast.LENGTH_SHORT).show()
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
    private fun bindObserverToGetBinStockTakeDetails(){
        binStockTakeViewModel.createShankyuReceive.observe(this, Observer {
            progressDialog.hide()

            when(it){
                is NetworkResult.Success->{
                    Log.d("Error","Success")
                    clearDataDisplay()
                    Toast.makeText(this,it.data,Toast.LENGTH_SHORT).show()
//                    if (it.data?.isEmpty() == true){
//                        binding.tvNoDataFound.isVisible=true
//                    }
//                    binStockTakeResponseFromApiAdapter = BinStockTakeResponseFromApiAdapter(it.data as ArrayList<BinStockResponseFromApiModel.BinStockResponseFromApiModelItem>)
//                    binding.listOfStockTake.adapter = binStockTakeResponseFromApiAdapter
//                    binding.ll.isVisible = true

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





    @SuppressLint("NotifyDataSetChanged")
    private fun clearDataDisplay() {
        adapter!!.clearTags()
        adapter!!.notifyDataSetChanged()
        refreshTotalTags()
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


    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {
                binding.apply {
//                    btnClear.isEnabled = false
//                    btnClear.setTextColor(getColor(R.color.white))
                    btnConfirm.isEnabled = false
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
                    btnConfirm.isEnabled = true
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

    private fun refreshTotalTags() {
        // Update ‘Total tags’.
        val storedTagCount = adapter!!.storedTagCount
        binding.noReadTags.text = storedTagCount.toString()
    }
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

    @SuppressLint("SetTextI18n")
    private fun dialogForTag() {
        dialogTag = Dialog(this)
        dialogTag.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogTag.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogTag.setContentView(R.layout.layout_for_confirmation)
        dialogTag.setCancelable(true)
        dialogTag.show()


        val cancel: MaterialButton = dialogTag.findViewById(R.id.btn_cancel)

        cancel.setOnClickListener {
            dialogTag.dismiss()

        }

        val yes: MaterialButton = dialogTag.findViewById(R.id.bt_yes)
        yes.setOnClickListener {
            if(scannedRfidTagNo.isNotEmpty()) {
               dialogTag.dismiss()
                //showProgressbar()
                binStockTakeViewModel.confirmReceiving(rfidList)
               bindObserverToGetBinStockTakeDetails()
            } else{
                Toast.makeText(this,"No item scanned",Toast.LENGTH_SHORT).show()
            }

            // disPatchItem(rfidListOfObject)



        }
    }


}