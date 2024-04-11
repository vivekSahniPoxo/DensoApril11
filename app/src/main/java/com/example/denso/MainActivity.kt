package com.example.denso


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Common.CommManager
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Dto.RFIDScannerSettings
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.densowave.scannersdk.RFID.RFIDException
import com.example.denso.ServiceParam.commScanner
import com.example.denso.bin_recieving.BinReceiving
import com.example.denso.bin_repair.BinRepairActivity
import com.example.denso.bin_scrap.BinScrap
import com.example.denso.bin_search.BinSearchActivity
import com.example.denso.bin_stock_take.StockTake
import com.example.denso.databinding.ActivityMainBinding
import com.example.denso.dispatch.DispatchActivity
import com.example.denso.internet_connection.ConnectionNetworkViewModel
import com.example.denso.settings.SettingActivity
import com.example.denso.shanku.ShankuReceivedActivity
import com.example.denso.shanku.ShankyuDispatchActivity
import com.example.denso.user_action.UserActivity
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.BeepAudioTracks
import com.example.denso.utils.Cons
import com.example.denso.utils.sharePreference.SharePref
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ScannerAcceptStatusListener, RFIDDataDelegate {
    lateinit var binding: ActivityMainBinding
    lateinit var baseActivity: BaseActivity
    var context: Context? = null

    lateinit var dialog:Dialog

    private val readHandler = Handler()

    private val hexString: String? = null

    private val TAG = "DEMO_SP1"
    val serviceKey = "serviceParam"
    private var locateTagState = LocateTagState.STANDBY
    private var scannerConnectedOnCreate = false
    private val readPowerLevelOnReadSearchTag: Int? = null

    private val networkViewModel: ConnectionNetworkViewModel by viewModels()

    lateinit var internetDialog: Dialog
    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        internetDialog = Dialog(this)
        baseActivity = BaseActivity()
        internetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharePref = SharePref()
        // Set the TOP-Activity
         baseActivity.setTopActivity(true)
        setContentView(binding.root)

        dialog = Dialog(this)


        // Service is started in the back ground.
        baseActivity.startService()
        scannerConnectedOnCreate = baseActivity.isCommScanner()

        binding.imLogout.setOnClickListener {
            dialogLogOut()

        }


        binding.mChangeBaseUrl.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intent)
            //finish()
        }

        binding.mCardViewSix.setOnClickListener {
            val intent = Intent(this@MainActivity, ShankuReceivedActivity::class.java)
            startActivity(intent)
        }

        binding.mCardViewSeven.setOnClickListener {
            val intent = Intent(this@MainActivity, ShankyuDispatchActivity::class.java)
            startActivity(intent)
        }



        binding.mCardView.setOnClickListener {
            val intent = Intent(this@MainActivity, DispatchActivity::class.java)
            //Toast.makeText(this,commScanner!!.btLocalName,Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }


        lifecycleScope.launchWhenCreated {
            networkViewModel.isConnected.collectLatest {
                if (it) {
                    internetDialog.dismiss()
                } else {
                    internetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    internetDialog.setContentView(R.layout.chek_internet_connection_layout)
                    internetDialog.setCancelable(false)
                    val ok  =  internetDialog.findViewById<TextView>(R.id.btRetry)
                    ok.setOnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS))
                        }
                    }
                    internetDialog.show()
                }
            }
        }

        binding.tvWaiting.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                if(binding.tvWaiting.text==Cons.WAITINGFORCONNECTION){
                    binding.imBluetooth.setBackgroundResource(R.drawable.ic_baseline_bluetooth_searching_24)
                } else {
                    if (binding.tvWaiting.text.substring(0,3)=="SP1")
                        binding.imBluetooth.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24)
                }
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {
                //binding.imBluetooth.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24)
            }
            override fun afterTextChanged(s: Editable) {



            }
        })








        //startReadSearchTag()



        binding.mCardViewOne.setOnClickListener {
            val intent = Intent(this@MainActivity, BinReceiving::class.java)
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            startActivity(intent)
        }

        binding.mCardViewTwo.setOnClickListener {
            val intent = Intent(this@MainActivity, BinScrap::class.java)
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            startActivity(intent)
        }

        binding.mCardViewThree.setOnClickListener {

            val intent = Intent(this@MainActivity, StockTake::class.java)
            val bundle = Bundle()
            bundle.putBoolean("isCommingFromStock",true)
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        binding.mCardViewFour.setOnClickListener {
            val intent = Intent(this@MainActivity, BinSearchActivity::class.java)
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            startActivity(intent)
        }

        binding.mCardViewFive.setOnClickListener{
            val intent = Intent(this@MainActivity, BinRepairActivity::class.java)
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            startActivity(intent)
        }


        BeepAudioTracks.setupAudioTracks(resources)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume ")
        // Connect it not connected when displaying the screen
        val connectionStatusTextView = binding.tvWaiting
        if (!baseActivity.isCommScanner()) {
            CommManager.addAcceptStatusListener(this)
            CommManager.startAccept()
            // Draw connection status if not connected
            connectionStatusTextView.text = getString(R.string.waiting_for_connection)
        } else {
            if (commScanner != null) {
                connectionStatusTextView.text = commScanner!!.btLocalName
                Toast.makeText(this,commScanner!!.btLocalName,Toast.LENGTH_LONG).show()
            } else {
                connectionStatusTextView.text = ""

            }
        }
    }





    override fun onDestroy() {

        // Release without forgetting AudioTrack
        BeepAudioTracks.releaseAudioTracks()
        if (baseActivity.isCommScanner()) {
            baseActivity.disconnectCommScanner()
        }
        // Abort the connection request
        CommManager.endAccept()
//        binding.imBluetooth.setBackgroundResource(R.drawable.ic_baseline_bluetooth_searching_24)
//        binding.tvWaiting.text = ""
        super.onDestroy()
    }

    override fun onUserLeaveHint() {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                CommManager.endAccept()
                CommManager.removeAcceptStatusListener(this@MainActivity)
                finish()
                return true
            }
        }
        return false
    }



    override fun OnScannerAppeared(mCommScanner: CommScanner) {
        var successFlag = false
        try {
            mCommScanner.claim()
            // Abort the connection request
            CommManager.endAccept()
            CommManager.removeAcceptStatusListener(this@MainActivity)
            successFlag = true
        } catch (e: CommException) {
            e.printStackTrace()
        }
        try {
            baseActivity.setConnectedCommScanner(mCommScanner)
            commScanner = baseActivity.getCommScanner()
            //commScanner = getCommScanner();
            // Displey BTLocalName of the connected scanner in the screen
            // Run on UI Thread using runOnUIThread
            val finalSuccessFlag = successFlag
            val btLocalName = commScanner!!.btLocalName
            runOnUiThread {
                val connectionStatusTextView = binding.tvWaiting
                if (finalSuccessFlag) {
                    connectionStatusTextView.text = btLocalName
                    Toast.makeText(this,btLocalName,Toast.LENGTH_LONG).show()
                } else {
                    connectionStatusTextView.text = getString(R.string.connection_error)
                }
            }

            // (STR) ADD CODE SHOW TOAST VERSION SP1 20181129
            // (STR) ADD CODE SHOW TOAST VERSION SP1 20181129
            if (commScanner!!.version.contains("Ver. ")) {
                val verSP1: String = commScanner!!.version.replace("Ver. ", "")
                val thread = Thread {
                    val text =
                        java.lang.String.format(getString(R.string.E_MSG_VERSION_SP1), verSP1)
                    if ("1.02" >= verSP1) {
                        runOnUiThread { baseActivity.showMessage(text) }
                    }
                }
                thread.start()
            }


        } catch (e: Exception) {
        }
    }


    private fun startReadSearchTag() {
        locateTagState = LocateTagState.READING_SEARCH_TAG

        // Disable UI operation other than the navigator while reading the tags used for searching
        //setEnableInteractiveUIWithoutNavigatorAndSearchTag(false)
        //setEnableSearchTag(false)

        // Set to the configured Read output value and open the inventory
        try {
            if (readPowerLevelOnReadSearchTag != null) {
                setScannerSettings(
                    readPowerLevelOnReadSearchTag,
                    RFIDScannerSettings.Scan.SessionFlag.S0, false
                )
            }
            openScannerInventory()
        } catch (e: CommException) {
            baseActivity.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        } catch (e: RFIDException) {
            baseActivity.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        }
    }


    @Throws(CommException::class, RFIDException::class)
    private fun openScannerInventory() {
        if (!scannerConnectedOnCreate) {
            return
        }
        baseActivity.getCommScanner()?.rfidScanner?.openInventory()
    }


    @Throws(CommException::class, RFIDException::class)
    private fun setScannerSettings(
        readPowerLevel: Int,
        @Nullable sessionFlag: RFIDScannerSettings.Scan.SessionFlag?,
        @Nullable sessionInit: Boolean?
    ) {
        if (!scannerConnectedOnCreate) {
            return
        }

        // Acquire only the configuration values from the existing ones
        val settings = baseActivity.getCommScanner()?.rfidScanner!!.settings

        // Set the value
        settings.scan.powerLevelRead = readPowerLevel

        // Set Polarization to Horizon
        settings.scan.polarization = RFIDScannerSettings.Scan.Polarization.H
        if (sessionFlag != null) {
            settings.scan.sessionFlag = sessionFlag
        }
        if (sessionInit != null) {
            settings.scan.sessionInit = sessionInit
        }
        baseActivity.getCommScanner()?.rfidScanner?.settings = settings
    }


    private fun runLocateTagAction(action: LocateTagAction) {
        when (action) {
            LocateTagAction.READ_SEARCH_TAG ->
                if (locateTagState == LocateTagState.STANDBY) {
                    startReadSearchTag()
                }

            else -> {

            }
        }
    }

    private enum class LocateTagAction {
        READ_SEARCH_TAG // Read the tags used for searching
        ,
        SEARCH_TAG // Retrieve the tags
        ,
        STOP;
        // Terminate the action
        /**
         * Convert to resource string
         *
         * @param resources  Resource for getting resource string
         * @return Resource string
         */
        fun toResourceString(resources: Resources): String {
            return when (this) {
                READ_SEARCH_TAG -> resources.getText(R.string.read).toString()
                SEARCH_TAG -> resources.getText(R.string.search).toString()
                STOP -> resources.getText(R.string.stop).toString()
                else -> throw IllegalArgumentException()
            }
        }
    }

    override fun onRFIDDataReceived(
        scanner: CommScanner?,
        rfidDataReceivedEvent: RFIDDataReceivedEvent
    ) {
        // Control between threads
        readHandler.post(Runnable {
            if (locateTagState == LocateTagState.READING_SEARCH_TAG) {
                readDataOnReadSearchTag(rfidDataReceivedEvent)
            } else if (locateTagState == LocateTagState.SEARCHING_TAG) {
                //readDataOnSearch(rfidDataReceivedEvent)
            }
        })
    }


    private fun readDataOnReadSearchTag(event: RFIDDataReceivedEvent) {
        // When two or more tags are read, set the UII of the first tag.
        val dataList = event.rfidData
        val firstData = dataList[0]
        try {
            //context?.let { TagUII.valueOf(firstData.uii, it) }?.let { setSearchTagUII(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Finish reading immediately after reading once
        // stopReadSearchTag()
    }


    // endregion
    // region Set/Get/Load search tag UII with UI
//    private fun setSearchTagUII(searchTagUII: TagUII) {
//        _searchTagUII = searchTagUII
//        val textView = binding.etTxt
//        textView.text = getHexString()
//    }


//    private fun getSearchTagUII(): TagUII? {
//        return _searchTagUII
//    }

    fun getHexString(): String {
        return hexString.toString()
    }



    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }


    @SuppressLint("SetTextI18n")
    private fun dialogLogOut() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.logout_dialog)
        dialog.setCancelable(true)
        dialog.show()


        val cancel: MaterialButton = dialog.findViewById(R.id.btnLogNo)

        cancel.setOnClickListener {
            dialog.dismiss()

        }

        val yes: MaterialButton = dialog.findViewById(R.id.btnLogOutYes)
        yes.setOnClickListener {
            dialog.dismiss()
            sharePref.logOut()
            val intent = Intent(this,UserActivity::class.java)
            startActivity(intent)
            finish()

            // disPatchItem(rfidListOfObject)



        }
    }



}