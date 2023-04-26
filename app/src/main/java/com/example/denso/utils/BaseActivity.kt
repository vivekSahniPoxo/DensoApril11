package com.example.denso.utils

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Common.CommScanner
import com.densowave.scannersdk.Common.CommStatusChangedEvent
import com.densowave.scannersdk.Const.CommConst.ScannerStatus
import com.densowave.scannersdk.Listener.ScannerStatusListener
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.ServiceParam
import com.example.denso.utils.Cons.Companion.serviceKey

/**
 * Common control class
 */
open class BaseActivity : AppCompatActivity(), ScannerStatusListener {
    private var ts: Toast? = null

    /**
     * TOP-Activity
     */
    private var topActivity = false
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add to Activity stack
        activityStack.add(this)
    }

     override fun onDestroy() {
        // Delete from Activity stack
        activityStack.remove(this)
        super.onDestroy()
    }

    /**
     * Set CommScanner which is connected
     * @param connectedCommScanner  Set CommScanner In case of CommScanner null which is connected, set the CommScanner which is being held to null.
     */
    fun setConnectedCommScanner(connectedCommScanner: CommScanner?) {
        if (connectedCommScanner != null) {
            scannerConnected = true
            connectedCommScanner.addStatusListener(this)
        } else {
            scannerConnected = false
            if (Companion.commScanner != null) {
                Companion.commScanner!!.removeStatusListener(this)
            }
        }
        Companion.commScanner = connectedCommScanner
    }

    /**
     * Get CommScanner
     * Since it is not always connected even if the acquired CommScanner is not null,
     * Use isCommScanner in order to check whether the scanner is connected
     * @return
     */


    /**
     * Determine CommScanner
     * If @return CommScanner is connected or disconnected, return true or false.
     */
    fun isCommScanner(): Boolean {
        return scannerConnected
    }

    /**
     * Disconnect SP1
     */
    fun disconnectCommScanner() {
        if (Companion.commScanner != null) {
            try {
                Companion.commScanner!!.close()
                Companion.commScanner!!.removeStatusListener(this)
                scannerConnected = false
                Companion.commScanner = null
            } catch (e: CommException) {
                showMessage(e.message)
            }
        }
    }

    /**
     * Display toast
     * @param msg
     */
    @Synchronized
    fun showMessage(msg: String?) {
        if (ts != null) {
            ts!!.cancel()
        }
        ts = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        ts!!.setGravity(Gravity.CENTER, 0, 0)
        ts!!.show()
    }

    /**
     * Start service in the background
     */
    fun startService() {
        if (isCommScanner()) {

            // Check if the service is already started or not

            val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val listServiceInfo = am.getRunningServices(Int.MAX_VALUE)
            for (curr in listServiceInfo) {
                // If it is up running, do not start the service again
                if (curr.service.className == TaskService::class.java.name) {
                    return
                }
            }

            // Start service
            val intent = Intent(application, TaskService::class.java)
            startService(intent)
            val serviceParam = ServiceParam
            ServiceParam.commScanner = commScanner
            intent.putExtra(serviceKey, serviceParam)


        }
    }

    fun getCommScanner(): CommScanner? {
        return commScanner
    }

    /**
     * Set TOP-Activity
     * @param topActivity true:TOP-Activity false:non TOP-Activity
     */
    fun setTopActivity(topActivity: Boolean) {
        this.topActivity = topActivity
    }

    /**
     * Event handling when the connection status of the scanner is changed
     * @param scanner Scanner
     * @param state Status
     */
    override fun onScannerStatusChanged(scanner: CommScanner, state: CommStatusChangedEvent) {
        // When the scanner is disconnected, commScanner will not be connected
        // Because this event handling is called asynchronously, if commScanner is set as null immediately, it may cause a null exception during processing
        // To prevent this, keep the instances and monitor the connection status using flags
        val scannerStatus = state.status
        if (scanner === commScanner && scannerStatus == ScannerStatus.CLOSE_WAIT) {
            // When disconnection status is detected, terminate all Activity other than those on the TOP screen
            this@BaseActivity.runOnUiThread(Runnable {
                if (scannerConnected) {
                    // Disconnection message display
                    try {
                        showMessage(getString(R.string.E_MSG_NO_CONNECTION))
                    } catch (e:Exception){

                    }
                    scannerConnected = false
                    for (i in activityStack.indices.reversed()) {
                        if (!activityStack[i].topActivity) {
                            // If the Activity is not on the TOP screen, delete Activity stack
                            activityStack[i].finish()
                        } else {
                            // If the Activity is on TOP screen, redraw Activity (onResume)
                            val intent = Intent(this@BaseActivity, this@BaseActivity.javaClass)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }
                    }
                }
            })
        }
    }

    companion object {
        var commScanner: CommScanner? = null
        var scannerConnected = false

        /**
         * Activity stack management
         */
        private val activityStack: MutableList<BaseActivity> = ArrayList()
    }


}