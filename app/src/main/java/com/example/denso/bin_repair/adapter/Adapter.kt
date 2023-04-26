package com.example.denso.bin_repair.adapter

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.R
import com.example.denso.bin_repair.adapter.BinRepainStatusAdapter.TagViewHolder

/**
 *
 * Class which controls the generation of RecyclerView which lists up tags
 *
 * The RecyclerView to which this class is applied has fixed width and height, and it shall be able to scroll when the tags does not fit in the display area
 *
 * In order to add a large amount of data, only a certain range of data can be displayed in the list
 *
 * When scrolling to the top and bottom of the list, if the position is not the start/end point of the entire data, then data in the range centered on that position is acquired
 *
 * The height of the RecyclerView to which this class is applied should be: [(height of one line + height of boundary line) * default number of tag lines to be displayed]
 *
 * TODO: Solve the problem of scrolling straight to the top when scrolling from the bottom and reading data in the range including the start point of the entire data
 *
 * TODO: Solve the problem of scrolling a large amount by just one tag when scrolling down and switching the display range (Check when adding multiple tags to one line)
 *
 */
class BinRepainStatusAdapter(private val context: Context, windowManager: WindowManager) :
    RecyclerView.Adapter<TagViewHolder>() {
    // Default number of tag lines for displaying 
    // Statically specify it as a constant by looking at the actual layout
    //private final int DEFAULT_TAG_LINE_NUMBER = 15;
    // Length of display range of tag
    private val TAG_SHOW_RANGE_LENGTH = 100
    private val tagDataSet: ArrayList<TagData>

    /**
     *
     * Get number of stored tags
     *
     * @return Number of tags
     */
    var storedTagCount = 0
        private set
    private var showTagRangeStart = 0

    // Context to acquire the color
    var defaultTagLineNumber = 15
        private set

    fun setDefaultTagLineNumber(windowManager: WindowManager) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        if (height < 1280) {
            defaultTagLineNumber = 11
        }
    }

    /**
     *
     * Constructor
     *
     * @param context Context to get color
     */
    init {
        tagDataSet = ArrayList()
        setDefaultTagLineNumber(windowManager)

        // Add a certain number of non-stored tags for displaying the boundary line
        for (i in 0 until defaultTagLineNumber) {
            tagDataSet.add(TagData())
        }
    }

    // Generate view (called from Layout Manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        // Create a new view
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.inventory_tag, parent, false
        )
        return TagViewHolder(v)
    }

    // Replace View content (called from Layout Manager)
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        // - get element from your data set at this position

        // - replace the contents of the view with that element


        // Set tag data.
        val tagDataIndex = showTagRangeStart + position
        holder.setTagData(tagDataSet[tagDataIndex])

        // To make it easy to see, change the background color alternately for each line
        val backgroundColorId = R.color.white
        holder.setBackgroundColor(context.getColor(backgroundColorId))
    }

    // Return the size of the data set (called from Layout Manager)
    override fun getItemCount(): Int {
        var itemCount = tagDataSet.size - showTagRangeStart
        if (itemCount > TAG_SHOW_RANGE_LENGTH) {
            itemCount = TAG_SHOW_RANGE_LENGTH
        }
        return itemCount
    }

    /**
     *
     * Add tag
     *
     * @param tagText Text of the added tag
     */
    fun addTag(tagText: String?) {
        // Set there in case there are unstored tags and add in case there are no stored tags.
        if (storedTagCount < tagDataSet.size) {
            tagDataSet[storedTagCount].setDataFromText(tagText)
        } else {
            tagDataSet.add(TagData(tagText))
        }
        ++storedTagCount

        // Count the number of tag rows
        var storedLine = 0
        var noStoredLine = 0
        for (tagData in tagDataSet) {
            if (tagData.isStore) {
                storedLine += tagData.lineNumber
            } else {
                noStoredLine += tagData.lineNumber
            }
        }

        // Delete extra unstored tag
        var overflowLine = storedLine + noStoredLine - defaultTagLineNumber
        val iterator = tagDataSet.listIterator(tagDataSet.size)
        while (iterator.hasPrevious()) {
            // Exit since there is no extra tag
            if (overflowLine <= 0) {
                break
            }

            // In case it is stored, exit since there is no unstored tag before.
            val tagData = iterator.previous()
            if (tagData.isStore) {
                break
            }

            // Delete because this is an extra unstored tag.
            iterator.remove()
            overflowLine -= tagData.lineNumber
        }
    }

    /**
     *
     * Clear tag
     *
     * The non-stored tag for displaying the boundary line is returned to the state where it is placed
     *
     */
    fun clearTags() {
        // Delete all tags at once
        tagDataSet.clear()

        // Add a certain number of non-stored tags for displaying the boundary line
        for (i in 0 until defaultTagLineNumber) {
            tagDataSet.add(TagData())
        }

        // Set number of store tag to “0” since there is no tag which is being stored. 
        storedTagCount = 0

        // Reset start position of display range to the default
        showTagRangeStart = 0
    }

    /**
     *
     * Whether the update of display range is required
     *
     * @param scrollPosition Scroll position
     *
     * @return True if the update of display range is required
     */
    fun needsRefreshShowRange(scrollPosition: Int): Boolean {
        // Verify whether it is necessary to read before the display range.
        val canLoadPrev = showTagRangeStart > 0
        if (canLoadPrev) {
            val atRangeStart = scrollPosition <= 0
            if (atRangeStart) {
                return true
            }
        }

        // Verify whether it is necessary to read after the display range.
        val canLoadNext = showTagRangeStart + TAG_SHOW_RANGE_LENGTH < tagDataSet.size - 1
        if (canLoadNext) {
            // It is necessary to update in case of the scroll position is already at the very back.

            // Enter here when the number of display tag lines fill the whole screen.
            if (scrollPosition == TAG_SHOW_RANGE_LENGTH - 1) {
                return true
            }

            // Calculate the number of lines from the next data of the scroll position to the end of the display range.
            var lineNumberAtScrollPositionsNextToRangeEnd = 0
            for (indexOnRange in TAG_SHOW_RANGE_LENGTH - 1 downTo scrollPosition + 1) {
                val indexOnAll = showTagRangeStart + indexOnRange
                lineNumberAtScrollPositionsNextToRangeEnd += tagDataSet[indexOnAll].lineNumber
            }
            val atRangeBottom = lineNumberAtScrollPositionsNextToRangeEnd <= defaultTagLineNumber
            if (atRangeBottom) {
                return true
            }
        }

        // It is unnecessary to update if the above conditions are not met.
        return false
    }

    /**
     *
     * Update display range if required
     *
     * @param scrollPosition Scroll position
     *
     * @return -1 if there is no new scroll position update
     */
    fun refreshShowRangeIfNeeded(scrollPosition: Int): Int {
        // Return “-1” if there is no update.
        val needsRefresh = needsRefreshShowRange(scrollPosition)
        if (!needsRefresh) {
            return -1
        }

        // Start position of display range ...[Length of display range of tag / 2]...  Make sure it is in the scroll position.
        val oldShowTagRangeStart = showTagRangeStart
        showTagRangeStart += scrollPosition - TAG_SHOW_RANGE_LENGTH / 2
        if (showTagRangeStart < 0) {
            showTagRangeStart = 0
        }

        // Find the new scroll position.
        val offset = showTagRangeStart - oldShowTagRangeStart
        return scrollPosition - offset
    }

    /**
     *
     * ViewHolder of the tag specified in RecyclerView
     *
     * If the text is configured as it is, there is a problem that a space is created under TextView according to the number of lines
     *
     * Solve the problem by adjusting line spacing on layout file
     *
     * If there is any way to arrange the text of multiple lines at exactly the same spacing, it should be implemented
     *
     */
    class TagViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val singleLineHeightDp = 28 // Row height (unit: dp)
        private val dividerHeightDp = 1 // Boundary line height (unit: dp)
        private val density // Need to convert from dp density to actual size
                : Float
        private val textTagView: TextView

        /**
         *
         * Constructor
         *
         * @param view The View assigned by RecyclerView
         */
        init {

            // Acquire density
            val displayMetrics = view.resources.displayMetrics
            density = displayMetrics.density
            textTagView = view.findViewById<View>(R.id.text_tag) as TextView
        }

        /**
         *
         * Set tag data
         *
         * @param data Tag data
         */
        fun setTagData(data: TagData) {
            // Change the height according to the number of lines so that all text can be displayed.

            // Being controlled by program side in order to ensure that the height of each View does not change even if there is a line break.
            val layoutParams = textTagView.layoutParams
            layoutParams.height = (density * (singleLineHeightDp * data.lineNumber +
                    dividerHeightDp * (data.lineNumber - 1))).toInt()
            textTagView.layoutParams = layoutParams

            // Set text to the tag
            textTagView.text = data.text
        }

        /**
         *
         * Set backgroung color
         *
         * @param backgroundColor Background color
         */
        fun setBackgroundColor(backgroundColor: Int) {
            textTagView.setBackgroundColor(backgroundColor)
        }
    }

    /**
     *
     * Data related to tags
     *
     */
    class TagData {
        private val MAX_LINE_CHAR_NUM = 24

        /**
         *
         * Get text
         *
         * @return Text
         */
        var text = "" // Text
            private set

        /**
         *
         * Get number of lines
         *
         * @return Number of lines
         */
        var lineNumber = 1 // Number of lines
            private set

        /**
         *
         * Whether it is stored or not
         *
         * @return Whether it is stored or not
         */
        var isStore = false // Whether it is stored or not
            private set

        /**
         *
         * Create empty data
         *
         */
        internal constructor() {
            setEmptyData()
        }

        /**
         *
         * Create data based on text
         *
         * @param sourceText The text to be the source
         */
        internal constructor(sourceText: String?) {
            setDataFromText(sourceText)
        }

        /**
         *
         * Empty data
         *
         */
        fun setEmptyData() {
            text = ""
            lineNumber = 1
            isStore = false
        }

        /**
         *
         * Set data based on text
         *
         * @param sourceText The text to be the source
         */
        fun setDataFromText(sourceText: String?) {
            // Wrap text at a certain number of characters.
            val textBuilder = StringBuilder(sourceText)
            var currentIndex = 0
            lineNumber = 1
            while (currentIndex + MAX_LINE_CHAR_NUM < textBuilder.length) {
                textBuilder.insert(currentIndex + MAX_LINE_CHAR_NUM, "\n")
                ++lineNumber
                currentIndex += MAX_LINE_CHAR_NUM + 1 /* \n == 1 */
            }
            text = textBuilder.toString()
            isStore = true
        }
    }
}