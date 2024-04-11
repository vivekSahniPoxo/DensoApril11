package com.example.denso.splash

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.example.denso.MainActivity
import com.example.denso.R
import com.example.denso.utils.sharPref.SharePref
import com.example.denso.user_action.UserActivity
import com.example.denso.utils.Cons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    lateinit var sharePref: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharePref = SharePref()
        sharePref.getData(Cons.BASE_URL)

        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.white)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (sharePref.getData(Cons.userId)!=null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }




//    Handler().postDelayed({
//        val sf = SharePref()
//        if (sf.getData(Cons.Token)!=null) {
//            Log.d("fetchToken", sf.getData(Cons.Token)!!)
//            val intent = Intent(this, HomeMainActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else {
//            val intent = Intent(this, UserActionActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//
//
//    }, 3000)

}

