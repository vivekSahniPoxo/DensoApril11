package com.example.denso.dispatch.dispatch_utils

import android.content.res.Resources
import com.example.denso.R

/**
 * Loading action
 */
 enum class ReadAction {
    START // Start reading
    ,
    STOP;
    // Stop reading
    /**
     * Convert to resource string
     *
     * @param resources  Resource for getting resource string
     * @return Resource string
     */
    fun toResourceString(resources: Resources): String {
        return when (this) {
            START -> resources.getText(R.string.start).toString()
            STOP -> resources.getText(R.string.stop).toString()
            else -> throw IllegalArgumentException()
        }
    }
}