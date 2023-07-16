package com.lingshot.common.helper

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lingshot.common.BuildConfig.ADMOB_INTERSTITIAL_ID
import javax.inject.Inject
import timber.log.Timber

class MobileAdsFacade @Inject constructor() {

    fun setupInterstitialAds(activity: Activity) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.w(adError.message)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    activity.showInterstitialAd(interstitialAd)
                    setupInterstitialAdListener(interstitialAd)
                }
            }
        )
    }

    private fun Activity.showInterstitialAd(interstitialAd: InterstitialAd) {
        interstitialAd.show(this)
    }

    private fun setupInterstitialAdListener(interstitialAd: InterstitialAd) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.w(adError.message)
            }
        }
    }
}
