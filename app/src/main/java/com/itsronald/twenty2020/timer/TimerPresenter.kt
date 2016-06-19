package com.itsronald.twenty2020.timer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.settings.SettingsActivity
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TimerPresenter
    @Inject constructor(override var view: TimerContract.TimerView)
    : TimerContract.UserActionsListener {

    /// Time that the long (work) cycle should take, in seconds.
    private val WORK_CYCLE_TIME  = Cycle.Phase.WORK.defaultDuration
    /// Time that the short (break) cycle should take, in seconds.
    private val BREAK_CYCLE_TIME = Cycle.Phase.BREAK.defaultDuration

    private var currentPhase = Cycle.Phase.WORK

    private val currentCycleName: String
        get() = if (currentPhase == Cycle.Phase.WORK) "work" else "break"

    private var running = false

    private var timeRemaining = WORK_CYCLE_TIME

    private var timeElapsed = 0

    private var secondsTimer = createSecondsTimer()

    private var timeLeftSubscription: Subscription? = null

    private var timerStringSubscription: Subscription? = null

    private var timerProgressMajorSubscription: Subscription? = null

    private fun createSecondsTimer(): Observable<Int> =
            Observable.interval(1, TimeUnit.SECONDS).take(timeRemaining).map { it.toInt() }

    private fun updateTime(): Observable<Int> = secondsTimer
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update time left.") }
            .doOnNext { timeElapsed = it }
            .doOnCompleted {
                Timber.i("${currentCycleName.toUpperCase()} cycle complete.")
                running = !running
                NotificationHelper(view.context).notifyPhaseComplete(currentPhase)
                startNextCycle()
            }

    private fun updateTimeText(): Observable<String> = secondsTimer
            .map { formatTimeRemaining(timeRemaining - it) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update time string.") }
            .doOnNext { view.showTimeRemaining(it) }
            .doOnCompleted { Timber.i("timerStringSubscription finished!") }

    private fun updateMajorProgressBar(): Observable<Int> = secondsTimer
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update major progress bar.") }
            .doOnNext { elapsedSeconds ->
                val cycleDuration        = if (currentPhase == Cycle.Phase.WORK) WORK_CYCLE_TIME else BREAK_CYCLE_TIME
                val majorProgressMax     = (cycleDuration / 60).toInt() // Minutes in work cycle
                val majorProgressCurrent = if (currentPhase == Cycle.Phase.WORK)
                    majorProgressMax - (elapsedSeconds / 60).toInt() else (elapsedSeconds / 60).toInt()
                Timber.d("Updating progress: $majorProgressCurrent / $majorProgressMax")
                view.showMajorProgress(majorProgressCurrent, majorProgressMax)
            }
            .doOnCompleted { Timber.i("Finished major progress bar update cycle.") }


    override fun onStart() {
        super.onStart()
        view.showTimeRemaining(formatTimeRemaining(timeRemaining))
    }

    private fun formatTimeRemaining(timeLeft: Int): String {
        val secondsLeft = timeLeft % 60
        val minutesLeft = (timeLeft / 60).toInt() % 60
        val hoursLeft   = (timeLeft / (60 * 60)).toInt()
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
        currentPhase  = currentPhase.nextPhase
        timeRemaining = if (currentPhase == Cycle.Phase.WORK) WORK_CYCLE_TIME else BREAK_CYCLE_TIME
        timeElapsed   = 0
        secondsTimer  = createSecondsTimer()
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
        timeLeftSubscription           = updateTime().subscribe()
        timerStringSubscription        = updateTimeText().subscribe()
        timerProgressMajorSubscription = updateMajorProgressBar().subscribe()

        view.setFABDrawable(android.R.drawable.ic_media_pause)
    }

    private fun pauseCycle() {
        timeLeftSubscription?.unsubscribe()
        timerStringSubscription?.unsubscribe()
        timerProgressMajorSubscription?.unsubscribe()

        timeRemaining -= timeElapsed
        secondsTimer = createSecondsTimer()

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