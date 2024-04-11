package com.example.denso.bin_scrap


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler

import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent

import com.example.denso.R
import com.example.denso.bin_scrap.bin_scrap_view_model.BinScrapViewModel
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.bin_stock_take.StockTake
import com.example.denso.bin_stock_take.adapter.TagRecyclerViewAdapter
import com.example.denso.bin_stock_take.bin_stock_take_view_model.BinStockTakeViewModel
import com.example.denso.databinding.ActivityBinScrapBinding


import com.example.denso.dispatch.RFIDNo
import com.example.denso.dispatch.ViewAllRfidTags
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.dispatch.model.RfidTag

import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import com.example.denso.utils.sharePreference.SharePref
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class BinScrap : BaseActivity(), RFIDDataDelegate {
    lateinit var binding: ActivityBinScrapBinding

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true

    lateinit var progressDialog: ProgressDialog

    lateinit var scannedRfidTagsNo:ArrayList<String>
    lateinit var dialogTag:Dialog

    private var isRefreshingShowRange = false

    private val binScrapViewModel:BinScrapViewModel by viewModels()

    private var adapter:TagRecyclerViewAdapter ? = null

    lateinit var scannedRfid:ArrayList<String>

    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinScrapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)

        scannedRfidTagsNo = arrayListOf()
        scannerConnectedOnCreate = super.isCommScanner()

        scannedRfid = arrayListOf()

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



        initRecyclerView()


        binding.btnView.setOnClickListener {

            val intent = Intent(this@BinScrap, StockTake::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Cons.RFID, (scannedRfid) as ArrayList<String>)
            bundle.putBoolean("isCommingFromScrep",true)
            intent.putExtras(bundle)
            startActivity(intent)
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

        binding.btnStartScraping.setOnClickListener {
            runReadActionTag()
        }

        binding.btnConfirmScraping.setOnClickListener {
            dialogForTag()

        }

//        binding.imBack.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
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

    private fun refreshTotalTags() {
        // Update ‘Total tags’.
        val storedTagCount = adapter!!.storedTagCount
        binding.noReadTags.text = storedTagCount.toString()
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



    private fun readData(rfidDataReceivedEvent: RFIDDataReceivedEvent) {

        for (i in rfidDataReceivedEvent.rfidData.indices) {
            var data = ""
            val uii = rfidDataReceivedEvent.rfidData[i].uii
            for (loop in uii.indices) {
                data += String.format("%02X ", uii[loop]).trim { it <= ' ' }
            }

            binding.tvScrap.text = data
            if (!scannedRfidTagsNo.contains(data)) {
                scannedRfidTagsNo.add(data)
                adapter!!.addTag(data)
                scannedRfid.add(data)
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
//                    btn.isEnabled = false
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
        nextReadAction =
            if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

        // For the buttons, set the name of the action to be executed next
        // val readToggle = findViewById<View>(R.id.button_read_toggle) as Button
        binding.btnStartScraping.text = nextReadAction.toResourceString(resources)
    }


    private fun bindObserverToGetResponseOfBinScrap(){
        binScrapViewModel.getResponseOfScrap.observe(this, Observer {
            progressDialog.hide()
            when(it){
                is NetworkResult.Success->{
                    binding.tvResponse.text = it.data
                    binding.noReadTags.text = ""
                    adapter?.clearTags()

                    binding.tvScrap.text = ""
                    scannedRfidTagsNo.clear()
                    scannedRfid.clear()
                    adapter?.notifyDataSetChanged()



                }
                is NetworkResult.Error->{
                    Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
                    Log.d("Error",it.message.toString())
                }
                is NetworkResult.Loading->{
                    Log.d("error","Loading")
                    showProgressbar()
                }

            }
        })

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
            dialogTag.dismiss()
            if (binding.tvScrap.text.isNotEmpty()) {
                val scrapRfidTag = binding.tvScrap.text.toString()
                for (i in scannedRfidTagsNo) {
                    val rfidTagNo = BinScrapModel("0x$i")
                    //val rfidTagNo = BinScrapModel("0x$scrapRfidTag")
                    Log.d("scrap", rfidTagNo.toString())
                    binScrapViewModel.binScrapFlow(rfidTagNo)
                    bindObserverToGetResponseOfBinScrap()
                }
                binding.noReadTags.text = ""
                adapter?.clearTags()


            } else{
                Toast.makeText(this,"Please scan tag",Toast.LENGTH_LONG).show()
            }

        }
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

}