package com.itsronald.twenty2020.timer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.BuildConfig
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.about.AboutPresenter
import com.itsronald.twenty2020.about.DaggerAboutComponent
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.data.ResourceRepository
import com.itsronald.twenty2020.feedback.FeedbackActivity
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.TimerControl
import com.itsronald.twenty2020.reporting.EventTracker
import com.itsronald.twenty2020.settings.SettingsActivity
import com.itsronald.twenty2020.timer.TimerContract.TimerView
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onError
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject


class TimerPresenter
    @Inject constructor(override var view: TimerContract.TimerView,
                        private val resources: ResourceRepository,
                        private val preferences: RxSharedPreferences,
                        private val cycle: Cycle,
                        private val eventTracker: EventTracker)
    : TimerContract.UserActionsListener, TimerControl by cycle {

    private val context: Context
        get() = view.context

    /**
     * Lazily instantiated presenter for the About screen.
     * We create the presenter here because the view is generated programatically by AboutLibraries
     * instead of by one of our own activities.
     */
    private val aboutPresenter: AboutPresenter by lazy {
        Timber.v("Building AboutPresenter")
        val resourceComponent = DaggerResourceComponent.builder()
                .resourceModule(ResourceModule(context))
                .build()
        val aboutComponent = DaggerAboutComponent.builder()
                .resourceComponent(resourceComponent)
                .build()
        aboutComponent.aboutPresenter()
    }

    //region Observers

    private lateinit var subscriptions: CompositeSubscription

    /**
     * Observe the most recent formatted time for the cycle.
     */
    private fun cycleTimeText(): Observable<String> = cycle.timer
            .map { it.remainingTime.toTimeString() }
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

    private fun cycleProgress(): Observable<Pair<Int, Int>> = cycle.timer
            .map { Pair(it.elapsedTime, it.duration) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to update major progress bar.") }

    private fun workProgress(): Observable<Pair<Int, Int>> = cycleProgress()
            .filter { cycle.phase == Cycle.Phase.WORK }

    private fun breakProgress(): Observable<Pair<Int, Int>> = cycleProgress()
            .filter { cycle.phase == Cycle.Phase.BREAK }

    private fun timerViewMode(): Observable<Long> = cycle.timer
            .map { timerModeForPhase(phase = it.phase) }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to switch TimerView mode.") }

    private fun timerModeForPhase(phase: Cycle.Phase): Long = when (phase) {
        Cycle.Phase.WORK  -> TimerContract.TimerView.TIMER_MODE_WORK
        Cycle.Phase.BREAK -> TimerContract.TimerView.TIMER_MODE_BREAK
    }

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
        updateTimeText()

        startSubscriptions()
        showTutorialOnFirstRun()
    }

    private fun updateTimeText() {
        val nextPhase = cycle.phase.nextPhase
        val nextDurationText = cycle.durationOfPhase(nextPhase).toTimeString()
        updateTimeTextForPhase(phase = nextPhase, timeText = nextDurationText)

        updateTimeTextForPhase(phase = cycle.phase, timeText = cycle.remainingTime.toTimeString())
    }

    private fun updateTimeTextForPhase(phase: Cycle.Phase, timeText: String) = when (phase) {
        Cycle.Phase.WORK  -> view.showWorkTimeRemaining(formattedTime = timeText)
        Cycle.Phase.BREAK -> view.showBreakTimeRemaining(formattedTime = timeText)
    }

    override fun onStop() {
        super.onStop()
        subscriptions.unsubscribe()
    }

    private fun startSubscriptions() {
        subscriptions = CompositeSubscription()

        subscriptions += cycleTimeText().subscribe {
            when (cycle.phase) {
                Cycle.Phase.WORK -> {
                    view.showBreakTimeRemaining(Cycle.Phase.BREAK.duration(resources).toTimeString())
                    view.showWorkTimeRemaining(it)
                }
                Cycle.Phase.BREAK -> {
                    view.showWorkTimeRemaining(Cycle.Phase.WORK.duration(resources).toTimeString())
                    view.showBreakTimeRemaining(it)
                }
            }
        }
        subscriptions += workProgress().subscribe { view.showWorkProgress(it.first, it.second) }
        subscriptions += breakProgress().subscribe { view.showBreakProgress(it.first, it.second) }
        subscriptions += timerViewMode().subscribe { view.timerMode = it }

        subscriptions += keepScreenOnPreference().subscribe { view.keepScreenOn = it }
        subscriptions += allowFullScreenPreference().subscribe { view.fullScreenAllowed = it }

        subscriptions += isCycleRunning().subscribe { running ->
            Timber.v("Switching play/pause icon.")
            view.setFABDrawable(if (running) R.drawable.ic_pause
                                else R.drawable.ic_play_arrow)
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
            Timber.i("Disabling options menu.")
            view.isMenuEnabled = false
        } else {
            Timber.v("Application was first launched as version $firstInstalledVersion. " +
                    "Skipping tutorial.")
            Timber.i("Enabling options menu.")
            view.isMenuEnabled = true
        }
    }

    override fun onTutorialNextClicked(currentState: Long) = view.showTutorial(when (currentState) {
        TimerView.TUTORIAL_TARGET_TIMER_START   -> TimerView.TUTORIAL_TARGET_TIMER_SKIP
        TimerView.TUTORIAL_TARGET_TIMER_SKIP    -> TimerView.TUTORIAL_TARGET_TIMER_RESTART
        TimerView.TUTORIAL_TARGET_TIMER_RESTART -> TimerView.TUTORIAL_NOT_SHOWN
        else -> throw IllegalArgumentException("$currentState is not a valid @TutorialState value.")
    })

    override fun onTutorialFinished() {
        Timber.v("Recording that the tutorial has been shown.")
        resources.savePreferenceString(
                keyResId = R.string.pref_nobackup_key_first_installed_version,
                stringToSave = BuildConfig.VERSION_NAME,
                prefsFilename = resources.getString(R.string.pref_filename_no_backup)
        )
        Timber.v("Re-enabling options menu.")
        view.isMenuEnabled = true
    }

    override fun onWorkTimerClicked() {
        if (cycle.phase == Cycle.Phase.WORK) {
            Timber.v("Ignoring work timer click - cycle is already in phase WORK.")
            return
        }
        startNextPhase()
    }

    override fun onBreakTimerClicked() {
        if (cycle.phase == Cycle.Phase.BREAK) {
            Timber.v("Ignoring break timer click - cycle is already in phase BREAK.")
            return
        }
        startNextPhase()
    }


    //endregion

    //region Menu interaction

    override fun openAboutApp() {
        eventTracker.reportEvent(EventTracker.Event.AboutAppClicked())

        val intent = aboutPresenter.buildIntent(context)
        context.startActivity(intent)
    }

    override fun openSettings() {
        Timber.i("Starting SettingsActivity.")
        eventTracker.reportEvent(EventTracker.Event.SettingsClicked())

        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    override fun openHelpFeedback() {
        Timber.i("Starting Help/Feedback activity.")
        eventTracker.reportEvent(EventTracker.Event.HelpFeedbackClicked())

        context.startActivity(Intent(context, FeedbackActivity::class.java))
    }

    //endregion

    //region TimerControl

    override fun toggleRunning() {
        eventTracker.reportEvent(
                if (cycle.running) EventTracker.Event.TimerPaused(cycle)
                else EventTracker.Event.TimerStarted(cycle)
        )

        cycle.toggleRunning()
    }

    override fun restartPhase() {
        eventTracker.reportEvent(EventTracker.Event.TimerRestarted(cycle))

        cycle.restartPhase()

        val message = resources.getString(R.string.timer_message_restarting_phase,
                cycle.phaseName.toLowerCase())
        view.showMessage(message = message)
    }

    override fun startNextPhase(delay: Int) {
        eventTracker.reportEvent(EventTracker.Event.TimerPhaseSkipped(cycle))

        cycle.startNextPhase(delay = delay)

        val message = resources.getString(R.string.timer_message_skip_to_next_phase,
                cycle.phaseName.toLowerCase())
        view.showMessage(message = message)
    }

    //endregion

    //region Formatting

    /**
     * A recyclable StringBuilder to use when formatting times.
     */
    private val timeStringBuilder = StringBuilder(8)

    /**
     * Format a time in seconds as HH:mm:ss when hours are present, mm:ss if the time is less
     * than an hour, or just as seconds if the time is less than a minute.
     */
    private fun Int.toTimeString(): String =
            if (this >= 60) DateUtils.formatElapsedTime(timeStringBuilder, this.toLong())
            else "$this"

    //endregion
}