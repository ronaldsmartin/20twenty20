package com.itsronald.twenty2020.timer

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.app.NotificationCompat
import com.itsronald.twenty2020.R
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

    companion object {
        /// Time that the long (work) cycle should take, in seconds.
        private val WORK_CYCLE_TIME: Int = 60
        /// Time that the short (break) cycle should take, in seconds.
        private val BREAK_CYCLE_TIME: Int = 30
    }

    private var inWorkCycle = true

    private val currentCycleName: String
        get() = if (inWorkCycle) "work" else "break"

    private var running = false

    private var timeRemaining = WORK_CYCLE_TIME

    private var timeElapsed = 0

    private var secondsTimer = createSecondsTimer()

    private var timeLeftSubscription: Subscription? = null

    private var timerStringSubscription: Subscription? = null

    private fun createSecondsTimer(): Observable<Int> =
            Observable.interval(1, TimeUnit.SECONDS).take(timeRemaining).map { it.toInt() }

    private fun updateTime(): Observable<Int> = secondsTimer
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update time left.") }
            .doOnNext { timeElapsed = it.toInt() }
            .doOnCompleted {
                Timber.i("${currentCycleName.toUpperCase()} cycle complete.")
                running = !running
                notifyCycleComplete()
                startNextCycle()
            }

    private fun updateTimeText(): Observable<String> = secondsTimer
            .map { formatTimeRemaining(timeRemaining - it) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update time string.") }
            .doOnNext { view.showTimeRemaining(it) }
            .doOnCompleted { Timber.i("timerStringSubscription finished!") }

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
        timeRemaining   = if (inWorkCycle) WORK_CYCLE_TIME else BREAK_CYCLE_TIME
        timeElapsed     = 0
        secondsTimer = createSecondsTimer()
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
        timeLeftSubscription = updateTime().subscribe()
        timerStringSubscription = updateTimeText().subscribe()

        view.setFABDrawable(android.R.drawable.ic_media_pause)
    }

    private fun pauseCycle() {
        timeLeftSubscription?.unsubscribe()
        timerStringSubscription?.unsubscribe()

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

    private fun notifyCycleComplete() {
        Timber.v("Building cycle complete notification")

        val context = view.context
        val contentText = if (inWorkCycle) "Time to take a break!" else "Get back to work!"
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${currentCycleName} cycle complete!")
                .setContentText(contentText)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLights(Color.WHITE, 1000, 100)

        val notificationID = 20
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notifyManager?.notify(notificationID, notificationBuilder.build())
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