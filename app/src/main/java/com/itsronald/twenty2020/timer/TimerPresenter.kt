package com.itsronald.twenty2020.timer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.TimerControl
import com.itsronald.twenty2020.notifications.CycleService
import com.itsronald.twenty2020.settings.SettingsActivity
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TimerPresenter
    @Inject constructor(override var view: TimerContract.TimerView, val cycle: Cycle)
    : TimerContract.UserActionsListener, TimerControl by cycle {

    private lateinit var subscriptions: CompositeSubscription

    /**
     * Updates the view's time text on each second tick.
     */
    private val timeStringUpdater = cycle.timer
            .map { it.remainingTimeText }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update time string.") }
            .doOnNext { view.showTimeRemaining(it) }

    /**
     * For each timer tick (one second apart), emits a series of new events mapping to an integer
     * percentage of progress. This increases the update rate of the progress indicator to smooth
     * out its animation.
     */
    private val progressBarUpdater = cycle.timer
            .concatMap { cycleState ->
                val isWorkPhase = cycleState.phase == Cycle.Phase.WORK
                val progress = if (isWorkPhase)
                    cycleState.duration - cycleState.elapsedTime else cycleState.elapsedTime
                val secondsPercent = progress.toDouble() / cycleState.duration

                val updateRateMilliseconds = 55
                val numChunks = 1000 / updateRateMilliseconds
                Observable.interval(updateRateMilliseconds.toLong(), TimeUnit.MILLISECONDS)
                        .take(numChunks)
                        .map {
                            val interpolation = it.toDouble() / numChunks / 50
                            secondsPercent + if (isWorkPhase) -interpolation else interpolation
                        }
                        .map { (it * 100).toInt() }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to update major progress bar.") }
            .doOnNext { view.showMajorProgress(it, 100) }

    override fun onStart() {
        super.onStart()
        view.showTimeRemaining(cycle.remainingTimeText)

        val context = view.context
        context.startService(Intent(context, CycleService::class.java))

        subscriptions = CompositeSubscription()
        subscriptions.add(timeStringUpdater.subscribe())
        subscriptions.add(progressBarUpdater.subscribe())
    }

    override fun onStop() {
        super.onStop()
        subscriptions.unsubscribe()
    }

    override fun toggleRunning() {
        if (running) {
            cycle.pause()
            view.setFABDrawable(android.R.drawable.ic_media_play)
        } else {
            cycle.start()
            view.setFABDrawable(android.R.drawable.ic_media_pause)
        }
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