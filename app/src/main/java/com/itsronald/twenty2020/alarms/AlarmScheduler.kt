package com.itsronald.twenty2020.alarms

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.TimerControl
import com.itsronald.twenty2020.timer.TimerActivity
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An [AlarmScheduler] schedules system alarm broadcasts based on the current state of the app
 * [Cycle]. Broadcasts are consumed by an [AlarmReceiver], which then posts system notifications
 * based on the scheduled alarm broadcast.
 *
 * Alarms longer than one minute are exposed to the user via the Alarm API. However, alarms shorter
 * than a minute are managed by a private [Subscription]. See [scheduleNextNotification] for details.
 *
 * There should only be one instance of [AlarmScheduler] active at any given time; since there is
 * only one type of broadcast, any additional [AlarmScheduler]s will override any alarms scheduled
 * by other instances.
 */
@Singleton
class AlarmScheduler
    @Inject constructor(val context: Context,
                        val alarmManager: AlarmManager,
                        val cycle: Cycle) {

    companion object {
        /** Request code for broadcast receivers to post a "cycle phase complete" notification. */
        private const val REQUEST_CODE_NOTIFY_PHASE_COMPLETE = 10

        /** Request code for receivers to show alarm controls. */
        private const val REQUEST_CODE_EDIT_ALARMS = 20

        /** Key used to pass the cycle phase for a "cycle phase complete" notification. */
        const val EXTRA_PHASE = "com.itsronald.alarms.extra.cycle_phase"
    }

    init {
        Timber.i("Scheduler created.")
    }

    /**
     * Observe changes to the cycle's timer state.
     */
    @Suppress("unused")
    private val timerEventSubscription = cycle.timerEvents()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext {
                Timber.e(it, "Encountered an error while observing TimerControl events.")
                cycle.timerEvents()
            }
            .subscribe { event ->
                Timber.i("Received timer event: ${TimerControl.eventName(event)}")
                updateAlarms()
            }


    //region Intents

    /**
     * The pending intent to schedule alarm broadcasts.
     * @see scheduleNotificationAlarm
     */
    private val alarmIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_NOTIFY_PHASE_COMPLETE,
                broadcastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )

    /**
     * The intent that will be broadcast during system alarms.
     * @see alarmIntent
     */
    private val broadcastIntent: Intent
        // While enums are Serializable and can be passed directly through an intent, it is
        // possible for a ClassNotFoundException to occur while de-serializing the extra.
        // Passing the name of the phase instead is a suitable workaround.
        // See http://stackoverflow.com/q/2307476/4499783 for more details.
        get() = Intent(AlarmReceiver.ACTION_NOTIFY)
                .putExtra(EXTRA_PHASE, cycle.phase.name)

    /**
     * An intent that will allow the user to edit alarms scheduled by this object.
     * In this case, the alarm control is the Cycle TimerControl exposed in TimerActivity.
     *
     * This intent is triggered when the user taps the next alarm time in the notification shade on
     * Android Marshmallow and above.
     *
     * @see scheduleNextNotification
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun buildShowAlarmIntent(): PendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_EDIT_ALARMS,
            TimerActivity.intent(context = context),
            0
    )

    //endregion

    /**
     * Update scheduled alarms based on the current state of the Cycle.
     *
     * If the Cycle is running, updates the scheduled alarm broadcast to the time of the cycle
     * phase's expiration; otherwise, cancels the scheduled alarm broadcast.
     */
    fun updateAlarms() {
        Timber.v("Updating alarms.")
        cancelNextNotification(cycle)
        if (cycle.running) scheduleNextNotification(cycle)
    }

    /**
     * Calculate the system time at which a cycle's current phase will expire.
     *
     * @param cycle The cycle for which to calculate the expiration time.
     * @return the system elapsed real time at which [cycle]'s phase will expire, in milliseconds.
     */
    private fun phaseExpirationTime(cycle: Cycle): Long = cycle.remainingTimeMillis +
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) System.currentTimeMillis()
            else SystemClock.elapsedRealtime()

    private fun phaseExpirationDate(cycle: Cycle): Date =
            Date(System.currentTimeMillis()+ cycle.remainingTimeMillis)

    /**
     * Schedule an alarm to notify the user of an upcoming cycle phase completion.
     *
     * @param cycle The cycle for which to schedule the alarm broadcast.
     */
    private fun scheduleNextNotification(cycle: Cycle) {
        // See: A flowchart for background work, alarms, and your Android app
        //      https://plus.google.com/+AndroidDevelopers/posts/GdNrQciPwqo
        scheduleNotificationAlarm(cycle = cycle)
    }

    private fun scheduleNotificationAlarm(cycle: Cycle) {

        val nextNotificationTime = phaseExpirationTime(cycle)
        val nextNotificationDate = phaseExpirationDate(cycle)

        Timber.i("Scheduling notification for phase ${cycle.phaseName} at time " +
                "$nextNotificationTime ($nextNotificationDate)")

        val alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Using AlarmManager.setAlarmClock() here instead of setExactAndAllowWhileIdle()
            // to prevent interference from Doze mode, which can defer alarms up to 15 minutes
            // when active and prevents alarms that are too frequent.
            alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(nextNotificationTime, buildShowAlarmIntent()),
                    alarmIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(alarmType, nextNotificationTime, alarmIntent)
        } else {
            alarmManager.set(alarmType, nextNotificationTime, alarmIntent)
        }
    }

    /**
     * Cancel any upcoming alarms for phase notifications.
     *
     * @param cycle The cycle whose phase completion will be used in the upcoming alarm.
     */
    private fun cancelNextNotification(cycle: Cycle) {
        Timber.i("Cancelling notification for phase ${cycle.phaseName}.")
        alarmManager.cancel(alarmIntent)
    }
}