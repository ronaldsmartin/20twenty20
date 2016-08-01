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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextNotificationTime, alarmIntent(cycle))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextNotificationTime, alarmIntent(cycle))
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextNotificationTime, alarmIntent(cycle))
        }
    }

    private fun cancelNextNotification(cycle: Cycle) {
        Timber.i("Cancelling notification for phase ${cycle.phaseName}.")
        alarmManager.cancel(alarmIntent(cycle = cycle))
    }

    private fun alarmIntent(cycle: Cycle): PendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_NOTIFY_PHASE_COMPLETE,
            broadcastIntent(cycle = cycle),
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun broadcastIntent(cycle: Cycle): Intent = Intent(context, AlarmReceiver::class.java)
            .putExtra(EXTRA_PHASE, cycle.phase)
}