package com.example.denso.bin_search

import android.content.res.Resources
import com.example.denso.R

/**
 * The action after finding the tag
 */
enum class LocateTagAction {
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