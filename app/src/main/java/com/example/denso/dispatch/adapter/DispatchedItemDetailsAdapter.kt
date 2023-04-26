import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.databinding.DispatchItemLayoutBinding
import com.example.denso.dispatch.event_listener.RFIDECheckListener
import com.example.denso.dispatch.model.BinDispatchDetails
import kotlin.math.log

class DispatchedItemDetailsAdapter(val rfidEventListener:RFIDECheckListener, private val mList: List<BinDispatchDetails.BinDispatchDetailsItem>) : RecyclerView.Adapter<DispatchedItemDetailsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DispatchItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = mList.size


    inner class ViewHolder(private val binding: DispatchItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mList: BinDispatchDetails.BinDispatchDetailsItem) {
            binding.apply {
                try {
                    Log.d("partName", mList.partName)
                    tvPartName.text = mList.partNo
                    tvLotSize.text = mList.lotSize.toString()
                    tvPkgPartNo.text = mList.groupName
                    tvCtn.text = mList.ctn.toString()
                    tvWeight.text = mList.weight.toInt().toString()
                    tvShipQty.text = mList.shipQty
                    for (i in mList.rfidNumber) {
                        Log.d("rfidAdapter",i.rfidTagNo)
                        rfidEventListener.onRfidListener(mList)
                    }




                } catch (e:Exception){

                }


            }

        }


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList[position].let { holder.bind(it) }
    }
}