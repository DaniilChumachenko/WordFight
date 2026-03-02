package com.chvma.wordfight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chvma.wordfight.ads.ActivityProvider
import com.google.android.gms.ads.MobileAds
import com.chvma.wordfight.speech.appContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        ActivityProvider.setActivity(this)
        appContext = applicationContext
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.setActivity(this)
    }
}
