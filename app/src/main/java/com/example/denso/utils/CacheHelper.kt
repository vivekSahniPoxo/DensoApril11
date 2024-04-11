package com.example.denso.utils

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import android.webkit.WebView
import java.io.File

object CacheHelper {

    // Clear cache for WebView
    fun clearWebViewCache(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
        } else {
            val cookieSyncManager = CookieSyncManager.createInstance(context)
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieSyncManager.sync()
        }

        WebStorage.getInstance().deleteAllData()
        WebView(context).clearCache(true)
    }

    // Clear application data including cache
    fun clearApplicationData(context: Context) {
        // Clear cache directory
        deleteDir(context.cacheDir)

        // Clear files directory
        deleteDir(context.filesDir)

        // Clear external cache directory
        if (isExternalStorageWritable()) {
            deleteDir(context.externalCacheDir)
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir?.delete() ?: false
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = android.os.Environment.getExternalStorageState()
        return android.os.Environment.MEDIA_MOUNTED == state
    }
}
