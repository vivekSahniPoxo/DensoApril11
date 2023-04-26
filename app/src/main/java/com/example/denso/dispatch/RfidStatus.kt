package com.example.denso.dispatch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.denso.R
import com.example.denso.databinding.FragmentRfidStatusBinding


class RfidStatus : Fragment() {
   lateinit var binding:FragmentRfidStatusBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rfid_status, container, false)
        binding = FragmentRfidStatusBinding.bind(view)
        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }


}