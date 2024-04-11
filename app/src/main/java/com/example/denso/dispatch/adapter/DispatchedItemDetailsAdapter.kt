import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.R
import com.example.denso.databinding.DispatchItemLayoutBinding
import com.example.denso.dispatch.event_listener.RFIDECheckListener
import com.example.denso.dispatch.model.BinDispatchDetails


class DispatchedItemDetailsAdapter(val rfidEventListener:RFIDECheckListener, val mList: ArrayList<BinDispatchDetails.BinDispatchDetailsItem>) : RecyclerView.Adapter<DispatchedItemDetailsAdapter.ViewHolder>() {

    //var scannedRfidTag: String = ""
    var scannedRfidTags: MutableList<String> = mutableListOf()
    val highlightedPositions = mutableSetOf<String>()


    var scannedTg = ""

//    var groupNameFromActivity = ""
//    var SilNoFromActivity = ""
//    var partNoFromActivity = 0
//    var isBackgroundColorGreen = false
//    var positionFromActivity = 0


    private var selectedItemPosition: Int = RecyclerView.NO_POSITION  // Initialize with an invalid position

    // ViewHolder class and binding methods go here

    // Create a method to set the selected item position
    fun setSelectedItemPosition(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged() // Notify the adapter to refresh the view
    }






    fun refreshAdapter() {
        notifyDataSetChanged()
    }

    fun clearItems() {
        uniqueItems.clear()
        notifyDataSetChanged()
    }

    private val uniqueItems: HashSet<BinDispatchDetails.BinDispatchDetailsItem> = HashSet()

    init {
        setItems(mList)
    }


    fun setItems(items: List<BinDispatchDetails.BinDispatchDetailsItem>) {
        uniqueItems.clear()
        uniqueItems.addAll(items)
        notifyDataSetChanged()
    }








      val tempList = arrayListOf<String>()
    private var filteredData: List<BinDispatchDetails.BinDispatchDetailsItem> = mList
   var query = ""
    fun search(query: String) {
        filteredData = if (query.isEmpty()) {
            mList
        } else {
            mList.filter { it.groupName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DispatchItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = uniqueItems.size




    inner class ViewHolder(private val binding: DispatchItemLayoutBinding):
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
        fun bind(mList: BinDispatchDetails.BinDispatchDetailsItem) {
            binding.apply {
                try {
                    tvPartName.text = mList.partNo
                    tvLotSize.text = mList.lotSize.toString()
                    tvPkgPartNo.text = mList.groupName
                    tvCtn.text = mList.ctn.toString()
                    //tvRfidNo.text = mList.rfidNumber.toString()




                    val rfidTags = mList.rfidNumber.map { it.rfidTagNo }
                    //Log.d("vivekemkn", rfidTags.joinToString(" "))
                    tvRfidNo.text = rfidTags.joinToString(" ")

                   // if (mList.partNo == groupNameFromActivity && mList){

//                    try {
//
//
//                        // Check if the current item matches the specified conditions for a green background
//                        if (mList.silNo == SilNoFromActivity && mList.groupName == groupNameFromActivity) {
//                            // Check if the background is not set or if it's not already green
//                            if (itemView.background == null || itemView.background.constantState?.equals(
//                                    ContextCompat.getDrawable(itemView.context, R.color.green2)?.constantState
//                                ) == false
//                            ) {
//                                // Change background color only if it's not already set or not green
//                                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
//                            }
//                        }
//                        else if (itemView.background != null) {
//                            // Reset background color only if it's set
//                            itemView.setBackgroundColor(Color.TRANSPARENT)
//                        }

//                    } catch (e: Exception) {
//                        // Handle exceptions that might occur during the binding process
//                    }

                        // Rest of your bindings...

                        // Set click listener for the item
                        itemView.setOnClickListener {
                            // Call the listener when the item is clicked
                            rfidEventListener.onRfidListener(mList, constraintLayout)

                        }





//                    if (mList.silNo == SilNoFromActivity && mList.ctn == partNoFromActivity){
//                       // Log.d("grrrrrr",groupNameFromActivity)
//                        tempList.add(mList.silNo)
//                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
//                        notifyDataSetChanged()
//                    } else if (!tempList.contains(mList.silNo)) {
//                        itemView.setBackgroundColor(Color.TRANSPARENT)
//                        notifyDataSetChanged()
//                    }


//                    if (positionFromActivity!=0) {
//                        changeBackgroundColor(itemView, positionFromActivity)
//                        notifyDataSetChanged()
//                    }


//                    if (position == selectedItemPosition){
//                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
//                    } else {
//                        itemView.setBackgroundColor(Color.TRANSPARENT)
//                    }




//                    if (mList.groupName == groupNameFromActivity && !isBackgroundColorGreen) {
//                        Log.d("grrrrrr", groupNameFromActivity)
//                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
//                        notifyDataSetChanged()
//                        isBackgroundColorGreen = true
//                    } else if (mList.groupName != groupNameFromActivity && isBackgroundColorGreen) {
//                        itemView.setBackgroundColor(Color.TRANSPARENT)
//                        notifyDataSetChanged()
//                        isBackgroundColorGreen = false
//                    }




                    val allMatched = if (rfidTags.isNotEmpty() && scannedRfidTags.isNotEmpty()) {
                        scannedRfidTags.containsAll(rfidTags)
                    } else {
                        false
                    }




//                    Log.d("scannedRfidTags", scannedRfidTags.toString())
//                    Log.d("rfidTags", rfidTags.joinToString(", "))
//                    Log.d("allMatched", "All RFID tags matched: $allMatched")


                    itemView.setOnClickListener {
                        rfidEventListener.onRfidListener(mList, constraintLayout)
                    }
                } catch (e:Exception){

                }


            }




        }



    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = uniqueItems.elementAt(position)
        holder.bind(item)

        if (highlightedPositions.contains(item.silNo) && highlightedPositions.contains(item.groupName.toString())) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green2))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }


    }


    private fun changeBackgroundColor(view: View, position: Int) {
        // Example: Change background color for even and odd positions
        val colorResId = if (position % 2 == 0) {
            R.color.green2
        } else {

            R.color.white
        }

        // Set the background color using a color resource
        view.setBackgroundResource(colorResId)
    }


}