package com.example.denso.bin_stock_take.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.bin_stock_take.model.BinStockResponseFromApiModel
import com.example.denso.databinding.BinStockTakeLayoutBinding




class BinStockTakeResponseFromApiAdapter(private val mList: ArrayList<BinStockResponseFromApiModel.BinStockResponseFromApiModelItem>) : RecyclerView.Adapter<BinStockTakeResponseFromApiAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = BinStockTakeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }


        override fun getItemCount(): Int = mList.size


        inner class ViewHolder(private val binding: BinStockTakeLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(mList: BinStockResponseFromApiModel.BinStockResponseFromApiModelItem) {
                binding.apply {

                    val hexString = mList.rfidNumber // Replace with your hexadecimal string

                    val byteArray = hexString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

                    val convertedString = String(byteArray, Charsets.UTF_8)




                    tvRfidTagNo.text = mList.binId
                    tvPartNo.text = mList.groupName
                    tvPartLotSize.text = mList.pkgLotSize.toString()


                }

            }

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            mList[position].let { holder.bind(it) }
        }


    fun clear (){
        mList.clear()
        notifyDataSetChanged()
    }
    }
