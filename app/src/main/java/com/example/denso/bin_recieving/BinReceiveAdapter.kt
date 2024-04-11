package com.example.denso.bin_recieving

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.R
import com.example.denso.databinding.DispatchItemLayoutBinding
import com.example.denso.databinding.InventoryTagBinding
import com.example.denso.dispatch.model.BinDispatchDetails



class BinReceiveAdapter(private val mList:ArrayList<RFidTagss>) : RecyclerView.Adapter<BinReceiveAdapter.ViewHolder>() {



    fun clearItems() {
        uniqueItems.clear()
        mList.clear()
        notifyDataSetChanged()
    }

    private val uniqueItems: HashSet<RFidTagss> = HashSet()

    init {
        setItems(mList)
    }


    fun setItems(items: List<RFidTagss>) {
        uniqueItems.clear()
        uniqueItems.addAll(items)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InventoryTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = uniqueItems.size




    inner class ViewHolder(private val binding: InventoryTagBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mList: RFidTagss) {
            binding.apply {
                try {
                    textTag.text = mList.rfid
                } catch (e:Exception){

                }


            }




        }



    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = uniqueItems.elementAt(position)
        holder.bind(item)



    }


}