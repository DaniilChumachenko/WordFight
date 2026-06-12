package com.chvma.wordfight

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chvma.pronounceWord.BuildConfig
import com.chvma.wordfight.ads.ActivityProvider
import com.chvma.wordfight.speech.appContext
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private var isSdkReady by mutableStateOf(false)
    private val isMobileAdsStarted = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ActivityProvider.setActivity(this)
        appContext = applicationContext

        gatherConsentThenInitAds()

        setContent {
            App(isSdkReady = isSdkReady)
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.setActivity(this)
    }

    /**
     * GDPR/UMP: shows the consent form where required and starts the Mobile Ads
     * SDK only once `canRequestAds()` is true, so no ad request ever goes out
     * without consent.
     */
    private fun gatherConsentThenInitAds() {
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode} ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds()
                    }
                }
            },
            { updateError ->
                // Offline or misconfigured; consent gathered on a previous
                // launch (if any) still allows ads.
                Log.w(TAG, "Consent info update failed: ${updateError.errorCode} ${updateError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds()
                }
            },
        )

        // Consent may already be granted from a previous launch; no need to
        // wait for the network round-trip before loading ads in that case.
        if (consentInformation.canRequestAds()) {
            initializeMobileAds()
        }
    }

    private fun initializeMobileAds() {
        if (!isMobileAdsStarted.compareAndSet(false, true)) return
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .apply {
                    if (BuildConfig.DEBUG) {
                        // Hashed id of the dev device (printed by the SDK in
                        // logcat). Debug builds get guaranteed test ads
                        // instead of "No fill" from the live ad units.
                        setTestDeviceIds(listOf("B8FD56F5243C9117E74F43D6D5EED151"))
                    }
                }
                .build(),
        )
        // SDK initialization is a known ANR source, keep it off the main thread.
        Thread {
            MobileAds.initialize(this) {
                runOnUiThread { isSdkReady = true }
            }
        }.start()
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
