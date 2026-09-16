package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dedicated manager for AdMob Interstitial ads in Split Roulette.
 *
 * Ensures:
 * - Thread-safe and lifecycle-safe ad loading and display.
 * - Exactly one interstitial opportunity per completed split at the natural transition.
 * - Zero delay, freeze, or error screen if an ad is unavailable or fails to show.
 * - Recomposition protection and duplicate-show protection.
 * - Automatic preloading of the next interstitial once consumed.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2252684615352337/9279175861"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isAdShowing = false
    private var isInitialized = false

    /**
     * Initializes the Google Mobile Ads SDK and triggers the first ad preload.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext
        try {
            MobileAds.initialize(appContext) { status ->
                Log.d(TAG, "MobileAds initialized: $status")
                preloadInterstitial(appContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    /**
     * Preloads an interstitial ad if one is not already loaded or loading.
     */
    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isLoading) {
            return
        }

        val appContext = context.applicationContext
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad successfully loaded")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message} (code: ${error.code})")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Shows the preloaded interstitial ad at the transition point.
     * If the ad is not loaded, fails to show, or activity is invalid, [onComplete] is called immediately.
     * When the ad is dismissed, [onComplete] is called and the next ad is preloaded.
     *
     * Guarded against duplicate calls and recomposition re-triggers.
     */
    fun showInterstitial(activity: Activity?, onComplete: () -> Unit) {
        // Fallback: If activity is null or dying, proceed immediately
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        // Fallback: If an ad is currently already on screen, do not show duplicate
        if (isAdShowing) {
            onComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            // Ad not ready: proceed immediately with no delay
            Log.d(TAG, "Interstitial ad not ready, continuing flow immediately")
            onComplete()
            preloadInterstitial(activity.applicationContext)
            return
        }

        // Consume the ad reference immediately so it cannot be used again
        interstitialAd = null
        isAdShowing = true

        val completionCalled = AtomicBoolean(false)
        fun notifyComplete() {
            if (completionCalled.compareAndSet(false, true)) {
                isAdShowing = false
                activity.runOnUiThread {
                    onComplete()
                }
                // Preload the next ad for future splits
                preloadInterstitial(activity.applicationContext)
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed by user")
                notifyComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                notifyComplete()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed full screen")
            }
        }

        try {
            ad.show(activity)
        } catch (e: Throwable) {
            Log.e(TAG, "Exception while showing interstitial ad", e)
            notifyComplete()
        }
    }
}

/**
 * Extension helper to reliably extract an Activity from a Compose Context.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
