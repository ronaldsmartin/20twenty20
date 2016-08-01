package com.itsronald.twenty2020.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.notifications.Notifier
import timber.log.Timber
import javax.inject.Inject

/**
 * A class responsible for scheduling user notifications in conjunction with system alarms.
 */
class AlarmScheduler
    @Inject constructor(val context: Context,
                        val alarmManager: AlarmManager,
                        val notifier: Notifier,
                        val cycle: Cycle) {

    companion object {
        /** Request code for broadcast receivers to post a "cycle phase complete" notification. */
        const val REQUEST_CODE_NOTIFY_PHASE_COMPLETE = 10

        /** Key used to pass the cycle phase for a "cycle phase complete" notification. */
        const val EXTRA_PHASE = "com.itsronald.alarms.extra.cycle_phase"
    }

    init {
        Timber.i("Scheduler created.")
    }

    //region Intents

    /**
     * The pending intent to schedule alarm broadcasts.
     */
    private val alarmIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_NOTIFY_PHASE_COMPLETE,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

    /**
     * The intent that will be broadcast during system alarms.
     */
    private val broadcastIntent: Intent
        get() = Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_PHASE, cycle.phase)

    //endregion

    fun updateAlarms() =
        if (cycle.running) scheduleNextNotification(cycle) else cancelNextNotification(cycle)

    /**
     * Calculate the system time at which a cycle's current phase will expire.
     *
     * @param cycle The cycle for which to calculate the expiration time.
     * @return the system elapsed real time at which [cycle]'s phase will expire, in milliseconds.
     */
    private fun phaseExpirationTime(cycle: Cycle): Long =
            SystemClock.elapsedRealtime() + cycle.remainingTimeMillis

    private fun scheduleNextNotification(cycle: Cycle) {
        val nextNotificationTime = phaseExpirationTime(cycle)
        Timber.i("Scheduling notification phase ${cycle.phaseName} at time $nextNotificationTime")

        val alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(alarmType, nextNotificationTime, alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(alarmType, nextNotificationTime, alarmIntent)
        } else {
            alarmManager.set(alarmType, nextNotificationTime, alarmIntent)
        }
    }

    private fun cancelNextNotification(cycle: Cycle) {
        Timber.i("Cancelling notification for phase ${cycle.phaseName}.")
        alarmManager.cancel(alarmIntent)
    }
}