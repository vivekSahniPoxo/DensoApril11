package com.example.denso.dispatch.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.denso.R
import com.example.denso.databinding.RfidStatusBinding

import com.example.denso.dispatch.model.RfidTag
import com.example.denso.utils.Cons

class AllScannedRfidStatusAdapter(private val mList: List<RfidTag>) : RecyclerView.Adapter<AllScannedRfidStatusAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RfidStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }


        override fun getItemCount(): Int = mList.size


        inner class ViewHolder(private val binding: RfidStatusBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("ResourceAsColor", "SuspiciousIndentation")
            fun bind(mList: RfidTag) {
                binding.apply {
                     tvRfidNo.text = mList.rfidTagNo
//                    if (mList.status=="1") {
//                        tvStatus.text = Cons.FOUND
//                        tvStatus.setBackgroundResource(R.drawable.green_bg)
//                        cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#00FF00")))
//
//                    } else{
                        if (mList.status=="2") {
                            tvStatus.text = Cons.FOUND
                            tvStatus.setBackgroundResource(R.drawable.green_bg)
                            cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF0000")))
                        }
                        else{
                            if (mList.status=="0") {
                                tvStatus.text = Cons.NOTFOUND
                                tvStatus.setBackgroundResource(R.drawable.blug_bg)
                                cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#0E86D4")))
                            }
                            else{
                                tvStatus.text = Cons.NOTFOUND
                                tvStatus.setBackgroundResource(R.drawable.red_bg)
                                cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF0000")))
                            }
//                        if (mList.status2=="dispatched" || mList.status2=="Dispatched"){
//                            tvStatus.text = Cons.DISPATCHED
//                            tvStatus.setBackgroundResource(R.drawable.yellow_bg)
//                            cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF0000")))
//                        } else{
//                            if (mList.status2.isEmpty()) {
//                                tvStatus.text = Cons.DISPATCHED
//                                tvStatus.setBackgroundResource(R.drawable.blug_bg)
//                                cardView.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF0000")))
//                            }
//                        }
                    }
                }


            }

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            mList[position].let { holder.bind(it) }
        }
    }