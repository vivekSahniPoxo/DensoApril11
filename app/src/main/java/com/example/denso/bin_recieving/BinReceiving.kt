package com.example.denso.bin_recieving

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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.utils.NetworkResult
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.bin_repair.model.BinRepairModel
import com.example.denso.bin_stock_take.adapter.BinStockTakeResponseFromApiAdapter
import com.example.denso.bin_stock_take.adapter.TagRecyclerViewAdapter
import com.example.denso.bin_stock_take.bin_stock_take_view_model.BinStockTakeViewModel
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import com.example.denso.bin_stock_take.model.BinStockTakeModel
import com.example.denso.databinding.ActivityBinRecievingBinding
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.dispatch.dispatchmodel.DispatchViewModel
import com.example.denso.dispatch.model.CreateRfidStatus
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BinReceiving : BaseActivity(),RFIDDataDelegate {
    lateinit var binding: ActivityBinRecievingBinding

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()

    private val binStockTakeViewModel: BinStockTakeViewModel by viewModels()

    private val dispatchViewModel : DispatchViewModel by viewModels()

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true

    lateinit var readRfidTags:ArrayList<String>

    private var isRefreshingShowRange = false




    lateinit var  rfidListOfObject:ArrayList<RfidTag>
    lateinit var scannedRfidTagNo:ArrayList<BinRepairModel>


    lateinit var binStockTakeResponseFromApiAdapter: BinStockTakeResponseFromApiAdapter

    lateinit var progressDialog: ProgressDialog

    // adapter for display scanned Rfid Tag no
    private var adapter: TagRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinRecievingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        readRfidTags = arrayListOf()

        scannedRfidTagNo = arrayListOf()
        rfidListOfObject  = arrayListOf()
        scannerConnectedOnCreate = super.isCommScanner()
        progressDialog = ProgressDialog(this)

        initRecyclerView()

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

        binding.btnView.setOnClickListener {
            binStockTakeViewModel.binStockTake(readRfidTags)
            bindObserverToGetBinStockTakeDetails()
        }

        binding.btnClear.setOnClickListener {
            binding.tvResponse.text = ""
            readRfidTags.clear()
            scannedRfidTagNo.clear()
            rfidListOfObject.clear()
            binStockTakeResponseFromApiAdapter.notifyDataSetChanged()
            adapter?.clearTags()
            adapter?.notifyDataSetChanged()
            scannedRfidTagNo.clear()
            binding.ll.isVisible  = false
            binding.noReadTags.text = ""
            clearDataDisplay()



        }


        binding.btnConfirmReceiving.setOnClickListener {
            val createStatus = CreateRfidStatus(rfidListOfObject)
            dispatchViewModel.confirmReceiving(scannedRfidTagNo)
            bindObserverToCreateRfidTagStatus()
        }

//        binding.imBack.setOnClickListener {
//             val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//        }


    }





    @SuppressLint("NotifyDataSetChanged")
    private fun clearDataDisplay() {
        adapter!!.clearTags()
        adapter!!.notifyDataSetChanged()
        refreshTotalTags()
    }
    private fun refreshTotalTags() {
        // Update ‘Total tags’.
        val storedTagCount = adapter!!.storedTagCount
        binding.noReadTags.text = storedTagCount.toString()
    }

    override fun onRFIDDataReceived(p0: CommScanner?, p1: RFIDDataReceivedEvent) {
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
               //readRfidTags.add("0x$data")
            try {
                adapter!!.addTag(data)
            }catch (e:Exception){

            }
            readRfidTags.add(data)

            val rfidTag = readRfidTags.distinct()
            binding.noReadTags.text = rfidTag.size.toString()
            scannedRfidTagNo.add(BinRepairModel(data,"3"))
                Log.d("readTag", "0x$data")
            //binding.tvScrap.text = data

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {


        // Specify this to improve performance since the size of RecyclerView is not changed
        val displayMetrics = DisplayMetrics()
        val params = binding.listOfRfidDetails.layoutParams
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        if (height < 1280) {
            params.height = (335 * Resources.getSystem().displayMetrics.density).toInt()
            binding.listOfRfidDetails.layoutParams = params
            binding.listOfRfidDetails.setHasFixedSize(true)
        }
        binding.listOfRfidDetails.setHasFixedSize(true)

        // Use LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        binding.listOfRfidDetails.layoutManager = layoutManager

        // Specify Adapter
        adapter = TagRecyclerViewAdapter(this, this.windowManager)
        binding.listOfRfidDetails.adapter = adapter

        // Receive the scroll event and the touch event.
        binding.listOfRfidDetails.addOnScrollListener(OnScrollListener())
        binding.listOfRfidDetails.addOnItemTouchListener(OnItemTouchListener())

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
                    binding.listOfRfidDetails.scrollToPosition(newScrollPosition)

                    // Finish updating the display range.
                    isRefreshingShowRange = false
                } catch (e: Exception) {
                    Log.d("DEMO_SP1", "Exception " + e.message)
                }
            }
        }
    }
    internal inner class OnItemTouchListener : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            // Do not allow to touch while updating the display range.
            return isRefreshingShowRange
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
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




    private fun bindObserverToGetBinStockTakeDetails(){
        binStockTakeViewModel.binStockTakeModelResponseLiveData.observe(this, Observer {
            progressDialog.dismiss()

            when(it){
                is NetworkResult.Success->{
                    Log.d("error","Success")
                   // clearDataDisplay()

                    binding.tvNoDataFound.isVisible = it.data?.isEmpty() == true

                        binStockTakeResponseFromApiAdapter =
                            BinStockTakeResponseFromApiAdapter(it.data as ArrayList<BinStockResponseFromApiModel.BinStockResponseFromApiModelItem>)
                        binding.listOfRfidDetails.adapter = binStockTakeResponseFromApiAdapter

                }

                is NetworkResult.Error->{
                    Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                    Log.d("error","error")

                }
                is NetworkResult.Loading->{
                    showProgressbar()
                    Log.d("error","Loading")

                }
            }

        })
    }

    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }



    private fun bindObserverToCreateRfidTagStatus(){
        dispatchViewModel.createDispatch.observe(this, Observer {
           progressDialog.dismiss()
            when(it){
                is NetworkResult.Success->{
                    //Toast.makeText(this,"SuccessFully Confirmed Dispatched",Toast.LENGTH_LONG).show()
                    binding.tvResponse.text = Cons.SuccessFully_Confirmed
                    scannedRfidTagNo.clear()
                }

                is NetworkResult.Error->{Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()}
                is NetworkResult.Loading->{
                    showProgressbar()
                } } })
    }

}