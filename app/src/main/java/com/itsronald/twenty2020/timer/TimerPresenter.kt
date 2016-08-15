package com.itsronald.twenty2020.timer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.BuildConfig
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.ResourceRepository
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.TimerControl
import com.itsronald.twenty2020.settings.SettingsActivity
import com.itsronald.twenty2020.timer.TimerContract.TimerView
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onError
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TimerPresenter
    @Inject constructor(override var view: TimerContract.TimerView,
                        val resources: ResourceRepository,
                        val preferences: RxSharedPreferences,
                        val cycle: Cycle)
    : TimerContract.UserActionsListener, TimerControl by cycle {

    private val context: Context
        get() = view.context

    //region Observers

    private lateinit var subscriptions: CompositeSubscription

    /**
     * Observe the most recent formatted time for the cycle.
     */
    private fun cycleTimeText(): Observable<String> = cycle.timer
            .map { it.remainingTimeText }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to update time string.") }

    /**
     * Watch changes to the cycle's running state.
     */
    private fun isCycleRunning(): Observable<Boolean> = cycle.timer
            .map { it.running }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to update time string.") }


    /**
     * For each timer tick (one second apart), emits a series of new events mapping to an integer
     * percentage of progress. This increases the update rate of the progress indicator to smooth
     * out its animation.
     */
    private fun cycleProgress(): Observable<Int> = cycle.timer
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
            .onError { Timber.e(it, "Unable to update major progress bar.") }

    /**
     * Watch changes to the user's display_keep_screen_on preference.
     */
    private fun keepScreenOnPreference(): Observable<Boolean> = preferences
            .getBoolean(resources.getString(R.string.pref_key_display_keep_screen_on))
            .asObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to observe KEEP_SCREEN_ON SharedPreference") }

    /**
     * Watch changes to the user's display_allow_full_screen preference.
     */
    private fun allowFullScreenPreference(): Observable<Boolean> = preferences
            .getBoolean(resources.getString(R.string.pref_key_display_allow_full_screen))
            .asObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to observe KEEP_SCREEN_ON SharedPreference") }

    //endregion

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Timber.i("Presenter created.")
    }

    override fun onStart() {
        super.onStart()
        view.showTimeRemaining(cycle.remainingTimeText)

        startSubscriptions()
        showTutorialOnFirstRun()
    }

    override fun onStop() {
        super.onStop()
        subscriptions.unsubscribe()
    }

    private fun startSubscriptions() {
        subscriptions = CompositeSubscription()

        subscriptions += cycleTimeText().subscribe { view.showTimeRemaining(it) }
        subscriptions += cycleProgress().subscribe { view.showMajorProgress(it, 100) }

        subscriptions += keepScreenOnPreference().subscribe { view.keepScreenOn = it }
        subscriptions += allowFullScreenPreference().subscribe { view.fullScreenAllowed = it }

        subscriptions += isCycleRunning().subscribe { running ->
            Timber.v("Switching play/pause icon.")
            view.setFABDrawable(if (running) android.R.drawable.ic_media_pause
                                else android.R.drawable.ic_media_play)
        }
    }

    //region Tutorial display

    private fun showTutorialOnFirstRun() {
        val firstInstalledVersion = resources.getPreferenceString(
                keyResId = R.string.pref_nobackup_key_first_installed_version,
                prefsFilename = resources.getString(R.string.pref_filename_no_backup)
        )
        if (firstInstalledVersion == null) {
            Timber.i("This is the first application launch. Showing tutorial.")
            view.showTutorial(TimerContract.TimerView.TUTORIAL_TARGET_TIMER_START)

            Timber.i("Recording that the tutorial has been shown.")
            resources.savePreferenceString(
                    keyResId = R.string.pref_nobackup_key_first_installed_version,
                    stringToSave = BuildConfig.VERSION_NAME,
                    prefsFilename = resources.getString(R.string.pref_filename_no_backup)
            )
        } else {
            Timber.v("Application was first launched as version $firstInstalledVersion. " +
                    "Skipping tutorial.")
        }
    }

    override fun onTutorialNextClicked(currentState: Long) = view.showTutorial(when (currentState) {
        TimerView.TUTORIAL_TARGET_TIMER_START   -> TimerView.TUTORIAL_TARGET_TIMER_SKIP
        TimerView.TUTORIAL_TARGET_TIMER_SKIP    -> TimerView.TUTORIAL_TARGET_TIMER_RESTART
        TimerView.TUTORIAL_TARGET_TIMER_RESTART -> TimerView.TUTORIAL_NOT_SHOWN
        else -> throw IllegalArgumentException("$currentState is not a valid @TutorialState value.")
    })


    //endregion

    //region Menu interaction

    override fun openAboutApp() {
        LibsBuilder()
                .withAppStyle()
                .withAboutOptions()
                .start(context)
    }

    private fun LibsBuilder.withAppStyle(): LibsBuilder {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = nightMode == Configuration.UI_MODE_NIGHT_YES

        val activityStyle = if (isNightMode) Libs.ActivityStyle.DARK
                            else Libs.ActivityStyle.LIGHT_DARK_TOOLBAR
        val activityTitle = resources.getString(R.string.about)

        return this.withActivityStyle(activityStyle)
                .withActivityTitle(activityTitle)
    }

    private fun LibsBuilder.withAboutOptions(): LibsBuilder {
        val appName = resources.getString(context.applicationInfo.labelRes)
        return this.withAboutAppName(appName)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withLicenseShown(true)
    }

    override fun openSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    override fun openHelpFeedback() {
        Timber.v("Opening Help/Feedback Chrome Custom Tab.")

        // Build the intent, customizing action bar color and animations.
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(resources.getColor(R.color.colorPrimary))
            .build()

        // Add referrer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse("${Intent.URI_ANDROID_APP_SCHEME}//${context.packageName}"))
        }

        // Open the custom tab.
        (context as? Activity)?.let {
            customTabsIntent.launchUrl(it, Uri.parse(resources.getString(R.string.help_feedback_url)))
        }
    }

    //endregion

    //region TimerControl

    override fun restartPhase() {
        cycle.restartPhase()

        val message = resources.getString(R.string.timer_message_restarting_phase,
                cycle.phaseName.toLowerCase())
        view.showMessage(message = message)
    }

    override fun startNextPhase(delay: Int) {
        cycle.startNextPhase(delay = delay)

        val message = resources.getString(R.string.timer_message_skip_to_next_phase,
                cycle.phaseName.toLowerCase())
        view.showMessage(message = message)
    }

    //endregion
}