package com.example.denso.user_action

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.databinding.ActivityUserBinding
import com.example.denso.dispatch.DispatchActivity
import com.example.denso.dispatch.model.BinDispatchDetails
import com.example.denso.dispatch.model.RfidTag
import com.example.denso.settings.SettingActivity
import com.example.denso.user_action.adapter.GetPlantResponseAdapter
import com.example.denso.user_action.model.PlantName
import com.example.denso.user_action.view_model.UserActionViewModel
import com.example.denso.utils.Cons
import com.example.denso.utils.NetworkResult
import com.example.denso.utils.sharePreference.SharePref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    lateinit var plantId:ArrayList<String>
    lateinit var binding: ActivityUserBinding
    private var selectedEmp: Int = 0
    private val userActionViewModel:UserActionViewModel by viewModels()
    lateinit var progressDialog:ProgressDialog
    lateinit var sharePref: SharePref

    private val handler = Handler(Looper.getMainLooper())

    lateinit var getPlantResponseAdapter: GetPlantResponseAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharePref = SharePref()

        progressDialog = ProgressDialog(this)

        plantId = arrayListOf()
        plantId.add("Choose")


        try {
            sharePref = SharePref()
            val savedBaseUrl = sharePref.getData("baseUrl")
            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = savedBaseUrl
                Log.d("baseURL", savedBaseUrl)
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }


//        try {
//            //if (Cons.BASE_URL.isEmpty()) {
//                if (sharePref.getData(Cons.BASE_URL).toString().isNotEmpty()) {
//                    Cons.BASE_URL = sharePref.getData(Cons.BASE_URL).toString()
//                //}
//            } else{
//                Cons.BASE_URL = "http://192.168.10.11:4555"
//            }
//        } catch (e:Exception){
//
//        }

//        if (sharePref.getData("userId")?.isNotEmpty() == true){
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else{
//            val intent = Intent(this,UserActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        binding.imSetting.setOnClickListener {
            val intent = Intent(this@UserActivity, SettingActivity::class.java)
            startActivity(intent)
            //finish()
        }

        userActionViewModel.getPlantName()
        bindObserverToGetPlantId()

        binding.btnLogin.setOnClickListener {
           if (binding.etUserId.text.toString().isNotEmpty() && binding.etUserId.text.toString().isNotEmpty()) {
               userActionViewModel.loginCredentials(binding.etUserId.text.toString(), binding.etPassword.text.toString(),"1")
               bindObserverForLogDetails()
           } else{
               Toast.makeText(this,"Please provide UserId and Password both",Toast.LENGTH_SHORT).show()
           }

        }


        binding.spPlant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                try {
                    val selectedEmpId = parent?.getItemAtPosition(position) as PlantName
                    selectedEmp = selectedEmpId[0].plantId

                } catch (e: Exception) {

                }



            }

        }

    }





    private fun bindObserverToGetPlantId(){
        userActionViewModel.getPlantNameResponseLiveData.observe(this, Observer {
            hideProgressbar()
            when(it){
                is NetworkResult.Success->{
                    //val plaintId = it.data?.get(0)?.plantName
//                    plantId.add(plaintId.toString())
//                    Toast.makeText(this,it.data?.get(0)?.plantName,Toast.LENGTH_SHORT).show()
//                    Log.d("plant",it.data?.get(0)?.plantId.toString())


                    getPlantResponseAdapter  = GetPlantResponseAdapter(this, it.data as List<PlantName.PlantNameItem>)
                    binding.spPlant.adapter = getPlantResponseAdapter

                }
                is NetworkResult.Error->{
                    Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading->{
                    showProgressbar()
                    handler.postDelayed({
                        hideProgressbar()
                        Toast.makeText(this,"Failed to connect", Toast.LENGTH_LONG).show()
                    },5000)
                }
            }
        })
    }



    private fun showProgressbar(){
        progressDialog.setMessage(Cons.loaderMessage)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideProgressbar(){
        progressDialog.hide()
    }



    private fun bindObserverForLogDetails(){
        userActionViewModel.userResponseLiveData.observe(this, Observer {
            hideProgressbar()
            when(it){
                is NetworkResult.Success->{
//                    val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
//                    val editor = sharedPreferences.edit()
//                    editor.putString("userID", binding.etUserId.text.toString())
//                    editor.apply()
                    sharePref.saveData(Cons.userId,binding.etUserId.text.toString())

                    val errorMessage = it.message
                    if (errorMessage != null) {
                        Toast.makeText(this@UserActivity, errorMessage, Toast.LENGTH_LONG).show()
                    } else {
                        // If the message is null, show a generic error message
                        Toast.makeText(this@UserActivity, "An error occurred", Toast.LENGTH_LONG).show()
                    }
                    //Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is NetworkResult.Error->{
                    hideProgressbar()
                    Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading->{
                    showProgressbar()
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        try {
            sharePref = SharePref()
            val savedBaseUrl = sharePref.getData("baseUrl")
            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = savedBaseUrl
                Log.d("baseURL", savedBaseUrl)
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        userActionViewModel.getPlantName()
        bindObserverToGetPlantId()
    }
















}