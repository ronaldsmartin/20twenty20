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
        private val WORK_CYCLE_TIME: Int = 60
        /// Time that the short (break) cycle should take, in seconds.
        private val BREAK_CYCLE_TIME: Int = 30
    }

    private var inWorkCycle = true

    private val currentCycleName: String
        get() = if (inWorkCycle) "WORK" else "BREAK"

    private var running = false

    private var timeRemaining = WORK_CYCLE_TIME

    private var timeElapsed = 0

    private var timerObservable = Observable.interval(1, TimeUnit.SECONDS).take(timeRemaining)

    private var timeLeftSubscription: Subscription? = null

    private var timerStringSubscription: Subscription? = null

    override fun onStart() {
        super.onStart()
        view.showTimeRemaining(formatTimeRemaining(timeRemaining))
    }

    private fun formatTimeRemaining(timeLeft: Number): String {
        val timeLeftSeconds  = timeLeft.toInt()
        val secondsLeft = timeLeftSeconds % 60
        val minutesLeft = (timeLeftSeconds / 60).toInt() % 60
        val hoursLeft   = (timeLeftSeconds / (60 * 60)).toInt()
        return when {
            hoursLeft > 0   -> {
                val minutes = "$minutesLeft".padStart(2, padChar = '0')
                val seconds = "$secondsLeft".padStart(2, padChar = '0')
                "$hoursLeft:$minutes:$seconds"
            }
            minutesLeft > 0 -> {
                val seconds = "$secondsLeft".padStart(2, padChar = '0')
                "$minutesLeft:$seconds"
            }
            else            -> "$secondsLeft"
        }
    }

    private fun startNextCycle() {
        inWorkCycle     = !inWorkCycle
        timeRemaining = if (inWorkCycle) WORK_CYCLE_TIME else BREAK_CYCLE_TIME
        timeElapsed     = 0
        timerObservable = Observable.interval(1, TimeUnit.SECONDS).take(timeRemaining)
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
        Timber.v("Starting $currentCycleName cycle. Time elapsed: $timeElapsed; Time left: $timeRemaining")
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
                .map { formatTimeRemaining(timeRemaining - it) }
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

        timeRemaining -= timeElapsed

        view.setFABDrawable(android.R.drawable.ic_media_play)
        Timber.v("Pausing cycle. Time elapsed: $timeElapsed; Time left: $timeRemaining")
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