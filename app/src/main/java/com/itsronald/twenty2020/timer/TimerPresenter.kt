package com.itsronald.twenty2020.timer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.settings.SettingsActivity
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onError
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TimerPresenter
    @Inject constructor(override var view: TimerContract.TimerView)
    : TimerContract.UserActionsListener {

    companion object {
        /// Time that the long (work) cycle should take, in seconds.
        private val WORK_CYCLE_TIME: Int = 20
        /// Time that the short (break) cycle should take, in seconds.
        private val BREAK_CYCLE_TIME: Int = 10
    }

    private var inWorkCycle = true

    private val currentCycleName: String
        get() = if (inWorkCycle) "WORK" else "BREAK"

    private var running = false

    private var totalCycleTime = WORK_CYCLE_TIME

    private var timeElapsed = 0

    private var timerObservable = Observable.interval(1, TimeUnit.SECONDS).take(totalCycleTime)

    private var timeLeftSubscription: Subscription? = null

    private var timerStringSubscription: Subscription? = null

    private fun startNextCycle() {
        inWorkCycle     = !inWorkCycle
        totalCycleTime = if (inWorkCycle) WORK_CYCLE_TIME else BREAK_CYCLE_TIME
        timeElapsed     = 0
        timerObservable = Observable.interval(1, TimeUnit.SECONDS).take(totalCycleTime)
        toggleCycleRunning()
    }

    override fun toggleCycleRunning() {
        running = !running
        if (running) {
            startCycle()
        } else {
            pauseCycle()
        }
    }

    private fun startCycle() {
        Timber.v("Starting $currentCycleName cycle. Time elapsed: $timeElapsed; Time left: $totalCycleTime")
        timeLeftSubscription = timerObservable
                .subscribeOn(Schedulers.computation())
                .onError { Timber.e(it, "Unable to update time left.") }
                .doOnCompleted {
                    Timber.i("$currentCycleName cycle complete.")
                    running = !running
                    startNextCycle()
                }
                .subscribe {
                    timeElapsed = it.toInt()
                }

        timerStringSubscription = timerObservable
                .map { "${totalCycleTime - it}" }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .onError { Timber.e(it, "Unable to update time string.") }
                .doOnCompleted {
                    Timber.i("timerStringSubscription finished!")
                }
                .subscribe { view.showTimeRemaining(it) }

        view.setFABDrawable(android.R.drawable.ic_media_pause)
    }

    private fun pauseCycle() {
        timeLeftSubscription?.unsubscribe()
        timerStringSubscription?.unsubscribe()

        totalCycleTime -= timeElapsed

        view.setFABDrawable(android.R.drawable.ic_media_play)
        Timber.v("Pausing cycle. Time elapsed: $timeElapsed; Time left: $totalCycleTime")
    }

    override fun delayCycle() {
        throw UnsupportedOperationException()
    }

    override fun restartCycle() {
        throw UnsupportedOperationException()
    }

    //region Menu interaction

    override fun openSettings() {
        val context = view.context
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    override fun openHelpFeedback() {
        Timber.v("Opening Help/Feedback Chrome Custom Tab.")

        // Build the intent, customizing action bar color and animations.
        val context = view.context
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .build()

        // Add referrer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse("${Intent.URI_ANDROID_APP_SCHEME}//${context.packageName}"))
        }

        // Open the custom tab.
        if (context is Activity) {
            customTabsIntent.launchUrl(context, Uri.parse(context.getString(R.string.help_feedback_url)))
        }
    }

    //endregion
}