package com.example.denso.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.denso.databinding.ActivitySettingBinding
import com.example.denso.utils.sharPref.SharePref
import com.example.denso.utils.CacheHelper
import com.example.denso.utils.Cons
import com.google.android.material.snackbar.Snackbar

class SettingActivity : AppCompatActivity() {
    lateinit var binding:ActivitySettingBinding
    lateinit var sharePref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            binding = ActivitySettingBinding.inflate(layoutInflater)
            setContentView(binding.root)
            sharePref = SharePref()

        binding.buttonSubmitUrl.setOnClickListener {
            sharePref.clearAll()
            CacheHelper.clearWebViewCache(this)
            CacheHelper.clearApplicationData(this)
            val updateBaseUrl = binding.baseUrlConfig.text.toString().trim()
            if (updateBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = "http://$updateBaseUrl"
                binding.ipconfigForm.visibility = View.GONE
                sharePref.saveData("baseUrl", Cons.BASE_URL)
                //showExitConfirmationDialog()
                Toast.makeText(this@SettingActivity, Cons.BASE_URL, Toast.LENGTH_SHORT).show()
                Snackbar.make(binding.root,"Please! close the application and reopen",Snackbar.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SettingActivity, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }




//        binding.buttonSubmitUrl.setOnClickListener {
//            //if (sharePref.getData(Cons.BASE_URL)?.isNotEmpty() == true){
//                sharePref.clearAll()
////            }else {
//                var UpdateBaseUrl = binding.baseUrlConfig.text.toString().trim()
//                Cons.BASE_URL = "http://$UpdateBaseUrl"
//                binding.ipconfigForm.visibility = View.GONE
//                sharePref.saveData("baseUrl", Cons.BASE_URL)
//                Toast.makeText(this@SettingActivity, Cons.BASE_URL, Toast.LENGTH_SHORT).show()
//            //}
//        }

        binding.ipconfig.setOnClickListener {
            binding.ipconfigForm.visibility = View.VISIBLE

        }
    }


    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please! close the application and reopen")
//            .setPositiveButton("Yes") { dialog, which ->
//                finish() // Close the activity and exit the application
//            }
//            .setNegativeButton("No") { dialog, which ->
//                // User clicked "No," do nothing, or handle as needed
//            }
            .show()
    }
}