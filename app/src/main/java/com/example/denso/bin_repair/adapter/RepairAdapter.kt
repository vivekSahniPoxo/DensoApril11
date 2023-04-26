package com.example.denso.bin_repair.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.R
import com.example.denso.bin_repair.model.RepairStatus
import com.example.denso.databinding.RapairLayoutBinding
import com.example.denso.databinding.RfidStatusBinding
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.utils.Cons



class RepairAdapter(private val mList: ArrayList<RepairStatus>) : RecyclerView.Adapter<RepairAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RapairLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int = mList.size


    inner class ViewHolder(private val binding: RapairLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(mList: RepairStatus) {
            binding.apply {
                tvRepair.text = mList.repairStatus


            }

        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList[position].let { holder.bind(it) }
    }
}