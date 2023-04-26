package com.example.denso.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.densowave.scannersdk.Common.CommException
import com.densowave.scannersdk.Common.CommManager
import com.densowave.scannersdk.Common.CommScanner
import com.example.denso.utils.Cons.Companion.serviceKey


class TaskService : Service() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // to do something
        commScanner = intent.getSerializableExtra(serviceKey) as CommScanner?
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Disconnect SP1
        close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        // Disconnect SP1
        close()
    }

    /**
     * Disconnect SP1
     */
    private fun close() {
        if (commScanner != null) {
            try {
                commScanner!!.close()
            } catch (e: CommException) {
            }
        }
        // Cancel connection request
        CommManager.endAccept()
    }

    companion object {
        private var commScanner: CommScanner? = null
    }
}