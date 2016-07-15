package com.itsronald.twenty2020.model

import android.content.Context
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Encapsulates the state of the repeating work and break cycle.
 */
class Cycle
    @Inject constructor(val context: Context,
                        val preferences: RxSharedPreferences)
    : TimerControl {

    /**
     * The alternating phases of the 20-20-20 cycle.
     */
    enum class Phase {
        /**
         * The longer phase of the cycle.
         */
        WORK,
        /**
         * The shorter phase of the cycle.
         */
        BREAK;

        /**
         * The total duration of this phase, in seconds.
         * @param context The context used to retrieve the preferred duration value from
         *                SharedPreferences.
         */
        fun duration(context: Context, preferences: RxSharedPreferences): Int {
            val preferredDurationID = when(this) {
                WORK  -> R.string.pref_key_general_work_phase_length
                BREAK -> R.string.pref_key_general_break_phase_length
            }
            val preferredDurationKey = context.getString(preferredDurationID)
            return preferences.getString(preferredDurationKey).get()?.toInt() ?: defaultDuration
        }

        /** The default duration for this phase. */
        val defaultDuration: Int
            get() = when(this) {
                WORK  -> 60 // 60 seconds
                BREAK -> 30 // 30 seconds
            }

        /** The next sequential phase that follows this phase. */
        val nextPhase: Phase
            get() = when(this) {
                WORK  -> BREAK
                BREAK -> WORK
            }

        /**
         * Retrieve the user-visible name for this phase.
         *
         * @param context The [Context] used to retrieve the localized name string.
         * @return The localized string name of this phase.
         */
        fun localizedName(context: Context): String = context.getString(when(this) {
            WORK  -> R.string.phase_name_work
            BREAK -> R.string.phase_name_break
        })
    }

    /** The current phase of the cycle. **/
    var phase = Phase.WORK
        private set

    /** Whether or not the cycle timer is currently running. */
    override var running = false
        private set

    /** The time that has elapsed current phase, in seconds. **/
    var elapsedTime = 0
        private set

    /** The total duration of the current phase, in seconds. **/
    var duration: Int = phase.duration(context, preferences)
        private set

    /** The time remaining in the current phase, in seconds. **/
    val remainingTime: Int
        get() = duration - elapsedTime

    /** Indicates whether the current phase time left is about to run out. */
    val isFinishingPhase: Boolean
        get() = elapsedTime == duration - 1

    /** PublishSubject where we update the timer state. **/
    private val timerSubject = PublishSubject.create<Cycle>().toSerialized()

    /** Observable timer for the current phase. */
    private var countdown: Subscription? = null

    /** Observable state of the cycle. */
    val timer: Observable<Cycle> = timerSubject.asObservable().onBackpressureLatest()

    //region Convenience properties

    /** The total number of minutes in the duration of the current phase. **/
    val durationMinutes: Int
        get() = duration / 60

    /** The number of minutes that have elapsed in the current phase. **/
    val elapsedTimeMinutes: Int
        get() = elapsedTime / 60

    /** The time left in the current phase, formatted as HH:mm:ss */
    val remainingTimeText: String
        get() {
            val secondsLeft = remainingTime % 60
            val minutesLeft = (remainingTime / 60).toInt() % 60
            val hoursLeft   = (remainingTime / (60 * 60)).toInt()
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


    //endregion

    //region TimerControl
    /**
     * Start the countdown for the current phase. If the phase countdown is already running, then
     * this command will be ignored.
     *
     * When the phase completes, the next phase will start automatically.
     *
     * @param delay If specified, the phase will wait [delay] seconds before starting.
     */
    fun start(delay: Int = 0) {
        if (running) {
            Timber.i("Cycle.start() ignored: the cycle is already running.")
            return
        }
        Timber.v("Starting ${phase.name} phase. Time elapsed: $elapsedTime; Time left: $remainingTime")

        countdown = Observable.interval(1, TimeUnit.SECONDS)
                .take(remainingTime)
                .delay(delay.toLong(), TimeUnit.SECONDS)
                .map { it.toInt() }
                .serialize()
                .subscribeOn(Schedulers.computation())
                .doOnError { timerSubject.onError(it) }
                .doOnCompleted {
                    running = !running
                    startNextPhase()
                }
                .subscribe {
                    elapsedTime += 1
                    if (timerSubject.hasObservers()) {
                        timerSubject.onNext(this)
                    }
                }

        running = true
    }

    /**
     * Pause the countdown for the phase in progress. If the phase countdown is not running, then
     * this command will be ignored.
     */
    fun pause() {
        if (!running) {
            Timber.i("Cycle.pause() ignored: the cycle is already paused.")
            return
        }
        Timber.v("Pausing ${phase.name} phase. Time elapsed: $elapsedTime; Time left: $remainingTime")
        countdown?.unsubscribe()
        running = false

        // Notify Observers that the Cycle has paused.
        if (timerSubject.hasObservers()) {
            timerSubject.onNext(this)
        }
    }

    override fun toggleRunning() = if (running) pause() else start()

    /**
     * Restart the current phase.
     */
    override fun restartPhase() {
        // Starting the elapsed time at -1 instead of 0 give us an extra second in which to display
        // the full unelapsed time.
        elapsedTime = 0
        duration = phase.duration(context, preferences)
        if (timerSubject.hasObservers()) {
            timerSubject.onNext(this)
        }
    }

    /**
     * Start the next phase.
     */
    override fun startNextPhase(delay: Int) {
        countdown?.unsubscribe()
        running = false
        phase   = phase.nextPhase
        restartPhase()
        start(delay)
    }

    //endregion
}