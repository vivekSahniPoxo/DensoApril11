package com.example.denso.bin_search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Dto.CommScannerParams
import com.densowave.scannersdk.Dto.CommScannerParams.Notification.Sound.Buzzer
import com.densowave.scannersdk.Dto.RFIDScannerSettings
import com.densowave.scannersdk.Dto.RFIDScannerSettings.Scan.Polarization
import com.densowave.scannersdk.Dto.RFIDScannerSettings.Scan.SessionFlag
import com.densowave.scannersdk.Listener.RFIDDataDelegate
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent
import com.densowave.scannersdk.RFID.RFIDException
import com.example.denso.*
import com.example.denso.dispatch.dispatch_utils.ReadAction
import com.example.denso.utils.BaseActivity
import com.example.denso.utils.BeepAudioTracks
import com.example.denso.utils.BeepAudioTracks.AudioTrackName
import com.example.denso.utils.StringInputFragment
import com.example.denso.utils.StringInputFragment.InputListener
import java.math.BigInteger
import java.util.*

class BinSearchActivity : BaseActivity(), RFIDDataDelegate {
    private val readPowerLevelOnReadSearchTag = 30
    private var readPowerLevelOnSearch: Float? = null
    private var readPowerStageOnSearch: ReadPowerStage? = null
    private var matchingMethod = MatchingMethod.FORWARD
    private var locateTagState = LocateTagState.STANDBY
    private val readHandler = Handler()
    private val messageHandler = Handler()
    private var settingPolarization: Polarization? = null
    private var settingSesionFlag: SessionFlag? = null
    private var settingReadPowLevel = 0
    var context: Context? = null
    private var nextReadAction = ReadAction.START

    // Whether it is connected to the scanner during generating time
    // Even when the connection is lost while on this screen, if it was connected to scanner during generating time, display the communication error
    private var scannerConnectedOnCreate = false
    private var disposeFlg = true
    var array_rssi = ArrayList<Short>()
    var searchTagToggle: Button? = null
    lateinit var commParams: CommScannerParams
    var buzzer_setting: Buzzer? = null
    lateinit var etBinId:EditText

    //    ImageView imBack;
    // endregion
    // region Activity relation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bin_search)
        scannerConnectedOnCreate = super.isCommScanner()
        commParams = CommScannerParams()

        etBinId = findViewById(R.id.et_binID)


//        imBack = findViewById(R.id.im_back);

        // When SP1 is not found, display the error message.
        if (!scannerConnectedOnCreate) {
            super.showMessage(getString(R.string.E_MSG_NO_CONNECTION))
        }
        searchTagToggle = findViewById<View>(R.id.button_search_tag_toggle) as Button
        // Set up the UI.
        //setupReadPowerLevelSpinner();

        // Reflect the tags UII used for searching which were saved last time
        loadSearchTagUII()

        // Since the UI has already been entered, Search button is enabled
        if (_searchTagUII != null) {
            // Enable UI operation in standby
            setEnableInteractiveUIWithoutNavigatorAndSearchTag(true)
            setEnableSearchTag(true)
        }




        // Register the listener
        if (scannerConnectedOnCreate) {
            try {
                super.getCommScanner()!!.rfidScanner.setDataDelegate(this)
            } catch (e: Exception) {
                super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            }
        }
        try {
            commParams = super.getCommScanner()!!.params
            buzzer_setting = commParams.notification.sound.buzzer
        } catch (e: Exception) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
        }
        saveSetting()
        context = this
        // Service is started in the back ground.
        super.startService()
    }

    override fun onDestroy() {
        BeepAudioTracks.stopAudioTracks()
        if (scannerConnectedOnCreate && disposeFlg) {
            super.disconnectCommScanner()
        }
        super.onDestroy()
    }

    override fun onRestart() {
        disposeFlg = true
        super.onRestart()
    }

     override fun onUserLeaveHint() {
        // Stop the reading and searching tags while a process is running in the background.
        runLocateTagAction(LocateTagAction.STOP)
        if (scannerConnectedOnCreate && locateTagState != LocateTagState.STANDBY) {
            disposeFlg = false
            runLocateTagAction(LocateTagAction.READ_SEARCH_TAG)
        }
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        when (keyCode) {
//            KeyEvent.KEYCODE_BACK -> {
//                // Transition the screen after reading and searching tags are stopped.
//                runLocateTagAction(LocateTagAction.READ_SEARCH_TAG)
//
//
//
//                // Transition the screen after the listener is released for registration.
//                if (scannerConnectedOnCreate) {
//                    super.getCommScanner()!!.rfidScanner.setDataDelegate(null)
//                }
//                commParams.notification.sound.buzzer = buzzer_setting
//                setting()
//                try {
//                    super.getCommScanner()!!.params = commParams
//                } catch (e: Exception) {
//                    super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
//                }
//
//                disposeFlg = false
//                finish()
//                return true
//            }
//        }
//        return false
//    }


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



                disposeFlg = false
                finish()
                runLocateTagAction(LocateTagAction.READ_SEARCH_TAG)
                return true
            }
        }
        return false
    }

    private fun runReadActionTag() {
        // Execute the configured reading action
        when (nextReadAction) {
            ReadAction.START -> {

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


            }
        }

        // Set the next reading action
        // Switch the previous reading action to STOP is it was STARTed, and to START if it was STOPped.
        nextReadAction =
            if (nextReadAction == ReadAction.START) ReadAction.STOP else ReadAction.START

        // For the buttons, set the name of the action to be executed next
//        binding.btnStartReading.text = nextReadAction.toResourceString(resources)
    }



    /**
     *
     * Move to the upper level at the time of screen transition
     *
     */
    private fun navigateUp() {
        // Transition the screen after reading and searching tags are stopped.
        runLocateTagAction(LocateTagAction.STOP)

        // Transition the screen after the listener is released for registration.
        if (scannerConnectedOnCreate) {
            super.getCommScanner()!!.rfidScanner.setDataDelegate(null)
        }
        if (commParams != null) {
            commParams!!.notification.sound.buzzer = buzzer_setting
            setting()
            try {
                super.getCommScanner()!!.params = commParams
            } catch (e: Exception) {
                super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            }
        }
        disposeFlg = false

        // Although there is such embedded navigation function "Up Button" in Android,
        // since it doesn't meet the requirement due to the restriction on UI, transition the the screen using button events.
        val intent = Intent(application, MainActivity::class.java)
        startActivity(intent)

        // Stop the Activity because it becomes unnecessary since the parent Activity is returned to.
        finish()
    }
    // endregion
    // region Handle click event
    /**
     * Processing when clicking
     * All the touch events in Activity are controlled by this
     * @param view Clicked View
     */
    fun onClick(view: View) {
        val id = view.id
        when (id) {
            R.id.button_read_search_tag ->{
                etBinId.isEnabled = false
                runLocateTagAction(LocateTagAction.READ_SEARCH_TAG)
            }
//            R.id.text_search_tag_uii_value -> editTagUII()
            R.id.radio_button_forward_match -> matchingMethod = MatchingMethod.FORWARD
            R.id.radio_button_backward_match -> matchingMethod = MatchingMethod.BACKWARD
            R.id.button_search_tag_toggle -> {
                val action =
                    if (locateTagState == LocateTagState.STANDBY) LocateTagAction.SEARCH_TAG else LocateTagAction.STOP
                runLocateTagAction(action)
            }
        }
    }
    // endregion
    // region Handle received RFID event
    /**
     * Processing when receiving data
     *
     * @param rfidDataReceivedEvent Reception event
     */
    override fun onRFIDDataReceived(
        scanner: CommScanner,
        rfidDataReceivedEvent: RFIDDataReceivedEvent
    ) {
        // Control between threads
        readHandler.post {
            if (locateTagState == LocateTagState.READING_SEARCH_TAG) {
                readDataOnReadSearchTag(rfidDataReceivedEvent)
            } else if (locateTagState == LocateTagState.SEARCHING_TAG) {
                readDataOnSearch(rfidDataReceivedEvent)
            }
        }
    }

    /**
     * Data reception processing when reading the data for the search
     *
     * @param event RFID reception event
     */
    private fun readDataOnReadSearchTag(event: RFIDDataReceivedEvent) {
        // When two or more tags are read, set the UII of the first tag.
        val dataList = event.rfidData
        val firstData = dataList[0]
        try {
            searchTagUII = TagUII.valueOf(
                firstData.uii,
                context
            )
        } catch (e: OverflowBitException) {
            e.printStackTrace()
        }

        // Finish reading immediately after reading once
        stopReadSearchTag()
    }

    /**
     * Data reception processing when searching
     *
     * @param event RFID reception event
     */
    private fun readDataOnSearch(event: RFIDDataReceivedEvent) {
        // Detect the data corresponding to the UII of search tag.
        val dataList = event.rfidData
        for (i in dataList.indices) {
            val data = dataList[i]
            if (Arrays.equals(data.uii, searchTagUII!!.bytes)) {
                ///if (checkMatchSearchTagUIIHexString(data.getUII())) {
                val rssi = data.rssi.toShort()
                array_rssi.add(rssi)
                Log.d("LocateTag rssi", "rssi=$rssi")
                Log.d("LocateTag polarization", "Polarization=" + data.polarization)


                //}
            } else {
                Log.d("LocateTag", getString(R.string.I_READ_UNSPECIFIED_UII))
            }
        }


        // Calculate the output value from RSSI and reflect to the display and the sound.
        // (Read output value [dBm])  =  (RSSI) / 10
        // Include RSSI to Short so that the value of 0x8000 or more becomes a negative value.
        // get average


        // Do nothing if there is no data that corresponds to the UII of the search tag.
        //if (array_rssi.size() == 0) {
        // return;
        //}

        // 1.5 seconds after reading, set the display to the initial state.
        // When the following reading is performed within 1.5 seconds, call removeCallbacksAndMessages and cancel.
        //messageHandler.removeCallbacksAndMessages(null);
        //setReadPowerLevelOnSearch(readPowerLevel);
    }

    var r: Runnable = object : Runnable {
        override fun run() {
            var readPowerLevel = 0f
            if (array_rssi.size == 0) {
                setReadPowerLevelOnSearch(null)
            } else {
                for (i in array_rssi.indices) {
                    readPowerLevel = readPowerLevel + array_rssi[i]
                }
                readPowerLevel = readPowerLevel / array_rssi.size / 10.0f
                setReadPowerLevelOnSearch(readPowerLevel)
                array_rssi.clear()
            }
            messageHandler.postDelayed(this, 250)
        }
    }

    /**
     * Returns whether the UII hexadecimal string of the search tag matches that of the search target tag
     * Even if it does not match as a byte array, it matches if it matches as a string
     *
     * Example: UII of search tag: "A12" UII of search target tag: "A 123" Matching method: In case of matching from the beginning,
     * If matching by byte array, the UII of the search tag does not match [0A, 12] and the UII of the search target tag does not match [A1, 23],
     * But since it is a matching by string, it is assumed that the part "A12" at the head matches and therefore coincides
     *
     * @param targetTagUII  Search tag UII
     * @return Return true if the UII hexadecimal string of the search tag matches that of the search target tag, and false if it does not match
     */
    private fun checkMatchSearchTagUIIHexString(targetTagUII: ByteArray): Boolean {
        // It is assumed to always be relevant since matching is not required when there is no UII of tag used for searching
        if (searchTagUII == null) {
            return true
        }

        // It is never relevant if the UII of the tags used for searching is longer than the UII to be searched.
        val searchTagUII = searchTagUII
        if (searchTagUII!!.bytes.size > targetTagUII.size) {
            return false
        }

        // It is assumed to relevant if the search string is at the beginning or the end of the string to be searched.
        val searchString = searchTagUII.hexString
        val targetString = bytesToHexString(targetTagUII)
        return when (matchingMethod) {
            MatchingMethod.FORWARD -> {
                val searchStringFirstIndex = targetString.indexOf(searchString)
                searchStringFirstIndex == 0
            }
            MatchingMethod.BACKWARD -> {
                val searchStringLastIndex = targetString.lastIndexOf(searchString)
                searchStringLastIndex == targetString.length - searchString.length
            }
            else -> false
        }
    }

    /**
     * Edit UII
     */
    private fun editTagUII() {
        val contents = StringInputContents()
        contents.title = getString(R.string.uii)
        contents.startString = if (searchTagUII != null) searchTagUII!!.hexString else ""
        contents.inputListener =
            InputListener { string -> // Set to blank for the null character string
                if (string.isEmpty()) {
                    searchTagToggle!!.isEnabled = false
                    searchTagUII = null
                    return@InputListener
                }


                // Verify the numerical values
                val uii: TagUII
                uii = try {
                    TagUII.valueOf(string, context)
                } catch (e: NotHexException) {
                    showMessage(getString(R.string.E_MSG_FILTER_INVALID_PATTERN))
                    return@InputListener
                } catch (e: OverflowBitException) {
                    showMessage(getString(R.string.E_MSG_FILTER_OUT_OF_RANGE_PATTERN))
                    return@InputListener
                }
                searchTagToggle!!.isEnabled = true
                // Perform the setting since the numerical value is a normal value
                searchTagUII = uii
            }
        showUpperAlphaInput(contents)
    }

    /**
     * Execute action after finding the tag
     * @param action The action after finding the tag
     */
    private fun runLocateTagAction(action: LocateTagAction) {
        when (action) {
            LocateTagAction.READ_SEARCH_TAG -> if (locateTagState == LocateTagState.STANDBY) {
                startReadSearchTag()
            }
            LocateTagAction.SEARCH_TAG -> if (locateTagState == LocateTagState.STANDBY) {
                startSearchTag()
            }
            LocateTagAction.STOP -> if (locateTagState == LocateTagState.READING_SEARCH_TAG) {
                stopReadSearchTag()
                etBinId.isEnabled = true
            } else if (locateTagState == LocateTagState.SEARCHING_TAG) {
                stopSearchTag()
                etBinId.isEnabled = true
            }
        }
    }

    /**
     * Save setting Polarization, when destroy this activity resetting Polarization
     */
    private fun saveSetting() {
        // Acquire only the configuration values from the existing ones
        try {
            val settings = super.getCommScanner()!!.rfidScanner.settings
            if (settingPolarization == null && settingReadPowLevel == 0 && settingSesionFlag == null) {
                settingPolarization = settings.scan.polarization
                settingReadPowLevel = settings.scan.powerLevelRead
                settingSesionFlag = settings.scan.sessionFlag
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save setting Polarization, when destroy this activity resetting Polarization
     */
    private fun setting() {
        // Acquire only the configuration values from the existing ones
        try {
            val settings = super.getCommScanner()!!.rfidScanner.settings
            settings.scan.polarization = settingPolarization
            settings.scan.powerLevelRead = settingReadPowLevel
            settings.scan.sessionFlag = settingSesionFlag
            super.getCommScanner()!!.rfidScanner.settings = settings
        } catch (e: RFIDException) {
            e.printStackTrace()
        }
    }

    /**
     * Start reading the search tag
     */
    private fun startReadSearchTag() {
        locateTagState = LocateTagState.READING_SEARCH_TAG

        // Disable UI operation other than the navigator while reading the tags used for searching
        setEnableInteractiveUIWithoutNavigatorAndSearchTag(false)
        setEnableSearchTag(false)

        // Set to the configured Read output value and open the inventory
        try {
            setScannerSettings(readPowerLevelOnReadSearchTag, SessionFlag.S0, false)
            openScannerInventory()
        } catch (e: CommException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        } catch (e: RFIDException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        }
    }

    /**
     * Stop reading the search tag
     */
    private fun stopReadSearchTag() {
        // Close the inventory
        try {
            closeScannerInventory()
        } catch (e: CommException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        } catch (e: RFIDException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        }

        // Enable UI operation in standby
        setEnableInteractiveUIWithoutNavigatorAndSearchTag(true)
        setEnableSearchTag(true)
        locateTagState = LocateTagState.STANDBY
    }

    /**
     * Start searching tag
     */
    private fun startSearchTag() {
        locateTagState = LocateTagState.SEARCHING_TAG

        // Disable the UI operation other than Navigator and Stop button (tag searching switch button) during tag searching
        setEnableInteractiveUIWithoutNavigatorAndSearchTag(false)

        // Set the name of the action to be stopped to the tag searching switch button
        searchTagToggle!!.text = LocateTagAction.STOP.toResourceString(
            resources
        )

        // Set the defined Read output value and session S0 and open the inventory
        //int searchReadPowerLevel = getResources().getInteger(R.integer.read_power_level_on_search_tag);
        val searchReadPowerLevel = 30
        val searchSessionFlag = SessionFlag.S0
        val uii: TagUII?
        val pass = "00000000"
        val addr: Short = 0
        array_rssi.clear()
        uii = searchTagUII
        try {
            setScannerSettings(searchReadPowerLevel, searchSessionFlag, false)
            //openScannerInventory();
            commParams!!.notification.sound.buzzer = Buzzer.DISABLE
            try {
                super.getCommScanner()!!.params = commParams
            } catch (e: Exception) {
                super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            }
            super.getCommScanner()!!.rfidScanner.openRead(
                RFIDScannerSettings.RFIDBank.UII,
                addr,
                uii!!.bytes.size.toShort(),
                stringToByte(pass),
                uii.bytes
            )
        } catch (e: CommException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        } catch (e: RFIDException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        }
        messageHandler.post(r)
    }

    /**
     * Stop searching tag
     */
    private fun stopSearchTag() {
        // Close the inventory
        try {
            closeScannerInventory()
        } catch (e: CommException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        } catch (e: RFIDException) {
            super.showMessage(getString(R.string.E_MSG_COMMUNICATION))
            e.printStackTrace()
        }

        // Enable UI operation in standby
        setEnableInteractiveUIWithoutNavigatorAndSearchTag(true)

        // Set the name of the tag searching action to the tag searching switch button
        val searchTagToggle = findViewById<View>(R.id.button_search_tag_toggle) as Button
        searchTagToggle.text = LocateTagAction.SEARCH_TAG.toResourceString(
            resources
        )

        // Eliminate the sound and the display of the Read output value
        setReadPowerLevelOnSearch(null)
        locateTagState = LocateTagState.STANDBY
        messageHandler.removeCallbacks(r)
    }

    /**
     * Open inventory of scanner
     * @throws CommException Exception concerning CommScanner
     * @throws RFIDException Exception concerning RFIDScanner
     */
    @Throws(CommException::class, RFIDException::class)
    private fun openScannerInventory() {
        if (!scannerConnectedOnCreate) {
            return
        }
        super.getCommScanner()!!.rfidScanner.openInventory()
    }

    /**
     * Close inventory of scanner
     * @throws CommException Exception concerning CommScanner
     * @throws RFIDException Exception concerning RFIDScanner
     */
    @Throws(CommException::class, RFIDException::class)
    private fun closeScannerInventory() {
        if (!scannerConnectedOnCreate) {
            return
        }
        super.getCommScanner()!!.rfidScanner.close()
    }

    /**
     * Set up the scanner
     * @param readPowerLevel  Read output value
     * @param sessionFlag  Session flag  Do not set the session flag if null is specified
     * @throws CommException Exception concerning CommScanner
     * @throws RFIDException Exception concerning RFIDScanner
     */
    @Throws(CommException::class, RFIDException::class)
    private fun setScannerSettings(
        readPowerLevel: Int,
        sessionFlag: SessionFlag?,
        sessionInit: Boolean?
    ) {
        if (!scannerConnectedOnCreate) {
            return
        }

        // Acquire only the configuration values from the existing ones
        val settings = super.getCommScanner()!!.rfidScanner.settings

        // Set the value
        settings.scan.powerLevelRead = readPowerLevel

        // Set Polarization to Horizon
        settings.scan.polarization = Polarization.H
        if (sessionFlag != null) {
            settings.scan.sessionFlag = sessionFlag
        }
        if (sessionInit != null) {
            settings.scan.sessionInit = sessionInit
        }
        super.getCommScanner()!!.rfidScanner.settings = settings
    }

    /**
     * Set enable/disable the UI operations other than navigator and tag search switching button
     * @param enabled Specify true if it is enabled, false if it is disabled
     */
    private fun setEnableInteractiveUIWithoutNavigatorAndSearchTag(enabled: Boolean) {
        // Disable/Enable the relevant UI
        //Spinner readPowerLevelSpinnerOnReadSearchTag = (Spinner) findViewById(R.id.spinner_power_level_value_on_read_search_tag);
        val readSearchTagButton = findViewById<View>(R.id.button_read_search_tag) as Button
        val searchTagUIIValueTextView =
            findViewById<View>(R.id.text_search_tag_uii_value) as TextView
        val forwardMatchRadioButton =
            findViewById<View>(R.id.radio_button_forward_match) as RadioButton
        val backwardMatchRadioButton =
            findViewById<View>(R.id.radio_button_backward_match) as RadioButton
        val interactiveViews = arrayOf<View>(
            readSearchTagButton, searchTagUIIValueTextView,
            forwardMatchRadioButton, backwardMatchRadioButton
        )
        for (interactiveView in interactiveViews) {
            interactiveView.isEnabled = enabled
        }


        // Display default text such that the basic color becomes enabled/disabled
        val colorIdBasedDefault = if (enabled) R.color.text_default else R.color.text_default_disabled
        val colorBasedDefault = getColor(colorIdBasedDefault)
        val textViews = arrayOf(
            readSearchTagButton, searchTagUIIValueTextView,
            forwardMatchRadioButton, backwardMatchRadioButton
        )
        for (textView in textViews) {
            textView.setTextColor(colorBasedDefault)
        }


        // Display highlighted text such that the basic color becomes enabled/disabled
        val colorIdBasedEnhanced =
            if (enabled) R.color.text_default_enhanced else R.color.text_default_disabled
        val colorBasedEnhanced = getColor(colorIdBasedEnhanced)
        //TextView tagUIITextHeadView = (TextView) findViewById(R.id.text_search_tag_uii_head);
        //tagUIITextHeadView.setTextColor(colorBasedEnhanced);
    }

    /**
     * Set enable/disable the tag search switching button
     * @param enabled Specify true if it is enabled, false if it is disabled
     */
    private fun setEnableSearchTag(enabled: Boolean) {
        val searchTagToggle = findViewById<View>(R.id.button_search_tag_toggle) as Button
        searchTagToggle.isEnabled = enabled
        val colorId = if (enabled) R.color.text_default else R.color.white
        searchTagToggle.setTextColor(getColor(colorId))
    }

    /**
     * Set the display and the sound of the Read output value when searching
     * @param readPowerLevel  Read output value when searching
     */
    private fun setReadPowerLevelOnSearch(readPowerLevel: Float?) {
        // If the Read output value is also null last time, do nothing
        if (readPowerLevelOnSearch == null && readPowerLevel == null) {
            return
        }
        readPowerLevelOnSearch = readPowerLevel

        // Reflect the changed Read output value on the display
        @SuppressLint("DefaultLocale") val text =
            if (readPowerLevel != null) String.format("%.2f", readPowerLevel) else "0.0"
        val textView = findViewById<View>(R.id.text_read_power_value_on_search) as TextView
        textView.text = text


        // If there is no change in the level of Read output value, do nothing
        val readPowerStage = getReadPowerStage(readPowerLevel)
        if (readPowerStageOnSearch == readPowerStage) {
            return
        }
        readPowerStageOnSearch = readPowerStage

        // Reflects the stage of Read output value after change in Yen and sound
        setSearchCircle(readPowerStage)
        setSearchSound(readPowerStage)
    }

    /**
     * Set display of circle according to the stage of Read output value
     * @param readPowerStage The stage of the Read output value that will be the basis of the circular display  Do not display if null is specified
     */
    private fun setSearchCircle(readPowerStage: ReadPowerStage?) {
        // Do not display if the stage of Read output value is null
        val imageView = findViewById<View>(R.id.image_search_circle) as ImageView
        if (readPowerStage != null) {
            imageView.visibility = ImageView.VISIBLE
        } else {
            imageView.visibility = ImageView.INVISIBLE
            return
        }

        // Configure the display of Yen according to the stage of Read output value
        val circleResId: Int
        circleResId =
            when (readPowerStage) {
                ReadPowerStage.STAGE_1 -> R.mipmap.locate_tag_circle_over_35
                ReadPowerStage.STAGE_2 -> R.mipmap.locate_tag_circle_48_to_36
                ReadPowerStage.STAGE_3 -> R.mipmap.locate_tag_circle_62_to_49
                ReadPowerStage.STAGE_4 -> R.mipmap.locate_tag_circle_74_to_63
                ReadPowerStage.STAGE_5 -> R.mipmap.locate_tag_circle_under_75
                else -> return
            }
        imageView.setImageResource(circleResId)
    }

    /**
     * Setting the sound according to the stage of Read output value
     * @param readPowerStage  The stage of Read output value  Do not playback if null is specified
     */
    private fun setSearchSound(readPowerStage: ReadPowerStage?) {
        // Stop audio track if the stage of Read output value is null
        if (readPowerStage == null) {
            BeepAudioTracks.stopAudioTracks()
            return
        }

        // Request the name of the audio track to play
        val playAudioTrackName: AudioTrackName = when (readPowerStage) {
                ReadPowerStage.STAGE_1 -> AudioTrackName.TRACK_1
                ReadPowerStage.STAGE_2 -> AudioTrackName.TRACK_2
                ReadPowerStage.STAGE_3 -> AudioTrackName.TRACK_3
                ReadPowerStage.STAGE_4 -> AudioTrackName.TRACK_4
                ReadPowerStage.STAGE_5 -> AudioTrackName.TRACK_5
                else -> return
            }

        // Terminate the the audio tracks which are not played
        val audioTrackNames = AudioTrackName.values()
        for (audioTrackName in audioTrackNames) {
            if (playAudioTrackName != audioTrackName) {
                BeepAudioTracks.stop(audioTrackName)
            }
        }

        // Play the relevant the audio track
        BeepAudioTracks.play(playAudioTrackName)
    }
    // endregion
    // region Set/Get/Load search tag UII with UI
    /**
     * Get UII of search tag
     * @return UII of search tag  Return null if there is no value
     *///Log.d("txt",searchTagUII != null ? searchTagUII.getHexString() : null);
    /**
     * Set UII of search tag
     * Reflect it in the corresponding TextView and Button
     * @param searchTagUII  UII of search tag If null is specified, it is kept as empty
     */
    private var searchTagUII: TagUII?
        private get() = _searchTagUII
        private set(searchTagUII) {
            _searchTagUII = searchTagUII
            val textView = findViewById<View>(R.id.text_search_tag_uii_value) as TextView
//            textView.text = searchTagUII?.hexString
            textView.text = stringToHex(etBinId.text.toString())

        }


    /**
     * Read search tag UII that is always kept during the execution of the application
     */
    private fun loadSearchTagUII() {
        searchTagUII = _searchTagUII
    }
    // endregion
    // region Dialog relation
    /**
     * Display StringInput dialog into which only uppercase alphanumeric characters can be entered
     * @param contents The contents displayed in the dialog
     */
    private fun showUpperAlphaInput(contents: StringInputContents) {
        val fragment = StringInputFragment()
        fragment.context = this
        fragment.inputType = StringInputFragment.InputType.UPPER_ALPHA_NUMERIC
        fragment.title = contents.title
        fragment.startString = contents.startString
        fragment.listener = contents.inputListener
        fragment.show(fragmentManager, getString(R.string.fragment_anonymous))
    }

    /**
     * Contents for generating StringInput dialog
     */
    private class StringInputContents {
        var title: String? = null
        var startString: String? = null
        var inputListener: InputListener? = null
    }
    // endregion
    // region Tag UII class
    /**
     * The class that shows UII of tag
     * The tag UII has restrictions of range and hexadecimal notation, so express it by wrapping
     */
    class TagUII {
        /**
         * Return as hexadecimal string
         * @return The hexadecimal string
         */
        // UII value (hexadecimal number)
        // UII extends to 256 bits ((0or1) * 256), so specify it by a character string
        var hexString: String
            private set

        /**
         * Return as a list of byte
         * @return The byte list
         */
        // Byte list UII value
        // Put in the list from the highest byte
        // For example, if the character string is "ABC", it becomes {0xA, 0xBC}
        var bytes: ByteArray
            private set

        /**
         * Initialize from hexadecimal string
         * @param hexString The hexadecimal string
         */
        private constructor(hexString: String) {
            this.hexString = hexString

            // Request byte list
            bytes = hexStringToBytes(hexString)
        }

        /**
         * Initialize from byte list
         * @param bytes Byte list
         */
        private constructor(bytes: ByteArray) {
            this.bytes = bytes

            // Find the hexadecimal string
            hexString = bytesToHexString(bytes)
        }

        companion object {
            private val hexCharacters = charArrayOf(
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
            )

            /**
             * Return tag UII based on hexadecimal string
             * @param hexString The hexadecimal string
             * @return Tag UII based on the specified hexadecimal string
             * @throws NotHexException When the specified string is not in  hexadecimal form
             * @throws OverflowBitException When the number of bits in the specified string exceeds the defined number of bits
             */
            @Throws(NotHexException::class, OverflowBitException::class)
            fun valueOf(hexString: String, context: Context?): TagUII {
                if (!checkHexString(hexString)) {
                    throw NotHexException(context)
                }
                if (hexString.length > 64 /* Up to 256 bits can be input, and 64 hexadecimal digits correspond to 256 bits */) {
                    throw OverflowBitException(256, context)
                }
                return TagUII(hexString)
            }

            /**
             * Return tag UII based on byte list
             * @param bytes Byte list
             * @return  Tag UII based on specified byte list
             * @throws OverflowBitException When the number of bits in the specified byte list exceeds the defined number of bits
             */
            @Throws(OverflowBitException::class)
            fun valueOf(bytes: ByteArray, context: Context?): TagUII {
                if (bytes.size > 32 /* Up to 256 bits can be input. Since 1 byte is 8 bits, 32 bytes correspond to 256 bits */) {
                    throw OverflowBitException(256, context)
                }
                return TagUII(bytes)
            }

            /**
             * Verify whether it is a hexadecimal string
             * @param string The string to be verified
             * @return True if it is a hexadecimal string. Otherwise, False
             */
            private fun checkHexString(string: String): Boolean {
                for (i in 0 until string.length) {
                    val character = string[i]
                    if (!checkHexCharacter(character)) {
                        return false
                    }
                }
                return true
            }

            /**
             * Verify whether it is a hexadecimal character
             * @param character The character to be verified
             * @return True if it is a hexadecimal character. Otherwise, False
             */
            private fun checkHexCharacter(character: Char): Boolean {
                for (hexCharacter in hexCharacters) {
                    if (character == hexCharacter) {
                        return true
                    }
                }
                return false
            }
        }
    }

    /**
     * This exception is thrown if the number of bits overflows
     */
    private class OverflowBitException
    /**
     * Initialize from number of bits
     * @param bitNumber The number of bits
     */
    internal constructor(bitNumber: Int, context: Context?) : Exception(
        String.format(
            Locale.getDefault(), context!!.getString(R.string.E_MSG_OVER_FLOW_BIT), bitNumber
        )
    )

    /**
     * This exception is thrown if the number is not in hexadecimal form
     */
    private class NotHexException
    /**
     * Initialize
     */
    internal constructor(context: Context?) :
        Exception(context!!.getString(R.string.E_MSG_NOT_HEXADECIMAL))
    // endregion
    // region Enums
    /**
     * Stage of Read output value (1 to 5 stage)
     */
    private enum class ReadPowerStage {
        STAGE_1, STAGE_2, STAGE_3, STAGE_4, STAGE_5
    }

    /**
     * Returns the stage corresponding to the Read output value
     * @param readPowerLevel  Read output value
     * @return  The stage corresponding to the Read output value Return null if null is specified to argument
     */
    private fun getReadPowerStage(readPowerLevel: Float?): ReadPowerStage? {
        if (readPowerLevel == null) {
            return null
        }
        val stage2MaxReadPowerLevel =
            resources.getInteger(R.integer.stage2_max_read_power_level_on_search)
        val stage3MaxReadPowerLevel =
            resources.getInteger(R.integer.stage3_max_read_power_level_on_search)
        val stage4MaxReadPowerLevel =
            resources.getInteger(R.integer.stage4_max_read_power_level_on_search)
        val stage5MaxReadPowerLevel =
            resources.getInteger(R.integer.stage5_max_read_power_level_on_search)
        return if (readPowerLevel <= stage5MaxReadPowerLevel) {
            ReadPowerStage.STAGE_5
        } else if (readPowerLevel <= stage4MaxReadPowerLevel) {
            ReadPowerStage.STAGE_4
        } else if (readPowerLevel <= stage3MaxReadPowerLevel) {
            ReadPowerStage.STAGE_3
        } else if (readPowerLevel <= stage2MaxReadPowerLevel) {
            ReadPowerStage.STAGE_2
        } else {
            ReadPowerStage.STAGE_1
        }
    }

    /**
     * Method of matching tag
     */
    private enum class MatchingMethod {
        FORWARD, BACKWARD
    }

    /**
     * The status after finding the tag
     */
    private enum class LocateTagState {
        STANDBY // Standing by
        ,
        READING_SEARCH_TAG // Reading the tags used for searching
        ,
        SEARCHING_TAG // Searching the tags
    }

    /**
     * The action after finding the tag
     */
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

    private fun stringToByte(hex: String): ByteArray {
        val bytes = ByteArray(hex.length / 2)
        for (index in bytes.indices) {
            bytes[index] = hex.substring(index * 2, (index + 1) * 2).toInt(16).toByte()
        }
        return bytes
    } // endregion

    companion object {
        // region Property
        // The tags UII used for searching is kept static so that they are retained during the execution of the application.
        // Set and get this variable using getter/setter method.
        private var _searchTagUII: TagUII? = null
        // endregion
        // region Convert hex string and bytes
        /**
         * Convert from hexadecimal string to byte list
         * @param hexString The hexadecimal string
         * @return The byte list based on the hexadecimal string
         */
        private fun hexStringToBytes(hexString: String): ByteArray {
            // Element 0 in case of null character
            if (hexString.length == 0) {
                return ByteArray(0)
            }

            // Cut out hexadecimal character string in byte unit and store it in the list
            // 1 byte is equivalent to 2 hexadecimal characters, so cut out 2 characters at a time
            // In order to cut out 2 characters at a time irrespective of the length of the character string, add 0 to the beginning if the character string is odd length
            val workHexString = if (hexString.length % 2 == 0) hexString else "0$hexString"
            val bytes = ByteArray(workHexString.length / 2)
            for (i in bytes.indices) {
                // If leave Byte.parseByte as it is, overflow will occur when the value becomes larger than 0x80
                // By parsing it to a larger type then casting it to byte, a value larger than 0x80 can be input as a negative value
                val hex2Characters = workHexString.substring(i * 2, i * 2 + 2)
                val number = String.format("%s", hex2Characters).toShort(16)
                bytes[i] = number.toByte()
            }
            return bytes
        }

        /**
         * Convert from byte list to hexadecimal string
         * @param bytes Byte list
         * @return The hexadecimal string based on the byte list
         */
        private fun bytesToHexString(bytes: ByteArray): String {
            val hexStringBuilder = StringBuilder()
            for (byteNumber in bytes) {
                val hex2Characters = String.format("%02X", byteNumber)
                hexStringBuilder.append(hex2Characters)
            }
            return hexStringBuilder.toString()
        }
    }



    fun stringToHex(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val bigInt = BigInteger(1, bytes)
        val hexString = bigInt.toString(16).toUpperCase()
        return hexString
    }
}