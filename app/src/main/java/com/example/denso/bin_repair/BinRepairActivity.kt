package com.example.denso.bin_repair

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
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
import com.example.denso.databinding.ActivityBinRepairBinding
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinRepairBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)

        scannedRfidTagNo  = arrayListOf()

        rfidTagList =  arrayListOf()

        repairOutList = arrayListOf()
        repairStatus =  arrayListOf()

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
                    binRepairViewModel.binRepairIn(scannedRfidTagNo)
                    binObserverBinRepair()

                }
                R.id.rd_out -> {
                    binRepairViewModel.binOutRepair(repairOutList)
                    bindObserverForOutRepairModel()
                }

            }
        }



    }


    private fun bindObserverForOutRepairModel(){
        binRepairViewModel.binOutRepairResponseLiveData.observe(this, Observer {
            progressDialog.hide()
            when(it){
                is NetworkResult.Success->{
                    scannedRfidTagNo.clear()
                    binding.tvResponse.text = "Repaired"
                    //binding.tvResponse.text = it.data.toString()
                    repairStatus.clear()
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
                    repairOutList.clear()
                    binding.tvResponse.text = "Repairing"
//                    binding.tvResponse.text = it.data.toString().replace("{", " ").replace("}", " ")
//                    val str = it.data.toString().replace("{", " ").replace("}", " ")
//                    Log.d("stsus",it.data.toString().replace("{", " ").replace("}", " "))
//                    Log.d("stsusOne",str.removeRange(0,3))
                   repairStatus.clear()

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

            binding.tvRfidNo.text = data
            scannedRfidTagNo.add(BinRepairModel(data,"Repair in"))
            repairOutList.add(BinRepairModel(data,"Repair out"))

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


}