package com.goldenowl.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.ui.tutorial.TutorialActivity
import com.goldenowl.ecommerce.utils.Utils.launchHome

class LaunchActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"
    private lateinit var userManager: UserManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        userManager = UserManager.getInstance(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setLaunchScreenTimeOut()
    }

    private fun setLaunchScreenTimeOut() {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                startActivity()
            }, TIME_OUT)
        }
    }

    private fun startActivity() {

        val settingsManager = SettingsManager(this)
        if (settingsManager.getFirstLaunch()) {
            val intentTutorial = Intent(this, TutorialActivity::class.java)
            startActivity(intentTutorial)
        } else {
            launchHome(this)
        }
        finish()
    }


    companion object {
        private const val TIME_OUT: Long = 1000
    }
}