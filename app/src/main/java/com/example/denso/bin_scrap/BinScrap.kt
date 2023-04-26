package com.example.denso.bin_scrap


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.bin_scrap.bin_scrap_view_model.BinScrapViewModel
import com.example.denso.bin_scrap.model.BinScrapModel
import com.example.denso.databinding.ActivityBinScrapBinding
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


@AndroidEntryPoint
class BinScrap : BaseActivity(), RFIDDataDelegate {
    lateinit var binding:ActivityBinScrapBinding

    private var nextReadAction = ReadAction.START
    private var handler: Handler? = Handler()

    private var scannerConnectedOnCreate = false
    private var disposeFlg = true

    lateinit var progressDialog: ProgressDialog

    lateinit var scannedRfidTagsNo:ArrayList<String>

    private val binScrapViewModel:BinScrapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinScrapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)

        scannedRfidTagsNo = arrayListOf()
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

        binding.btnStartScraping.setOnClickListener {
            runReadActionTag()
        }

        binding.btnConfirmScraping.setOnClickListener {
            if (binding.tvScrap.text.isNotEmpty()) {
                val scrapRfidTag = binding.tvScrap.text.toString()
                val rfidTagNo = BinScrapModel("0x$scrapRfidTag")
                Log.d("scrap",rfidTagNo.toString())
                binScrapViewModel.binScrapFlow(rfidTagNo)
                bindObserverToGetResponseOfBinScrap()
            } else{
                Toast.makeText(this,"Please scan tag",Toast.LENGTH_LONG).show()
            }
        }

//        binding.imBack.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
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

            binding.tvScrap.text = data
            scannedRfidTagsNo.add(data)

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
        binding.btnStartScraping.text = nextReadAction.toResourceString(resources)
    }


    private fun bindObserverToGetResponseOfBinScrap(){
        binScrapViewModel.getResponseOfScrap.observe(this, Observer {
            progressDialog.hide()
            when(it){
                is NetworkResult.Success->{
                    binding.tvResponse.text = it.data
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

}