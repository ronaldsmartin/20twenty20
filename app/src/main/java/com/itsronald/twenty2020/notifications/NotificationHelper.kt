package com.itsronald.twenty2020.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import android.support.v7.app.NotificationCompat
import android.text.format.DateUtils
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.timer.TimerActivity
import com.itsronald.twenty2020.timer.TimerContract
import timber.log.Timber
import javax.inject.Inject


class NotificationHelper(private val context: Context) {

    companion object {
        private val ID_PHASE_COMPLETE = 20
    }

    @Inject lateinit var preferences: RxSharedPreferences

    /***
     * Build a new notification indicating that the current phase is complete.
     *
     * @param phaseCompleted The phase that was completed.
     * @return a new notification for posting
     */
    private fun phaseCompleteNotification(phaseCompleted: Cycle.Phase): Notification {
        val titleID = if (phaseCompleted == Cycle.Phase.WORK)
                R.string.notification_title_work_cycle_complete
            else R.string.notification_title_break_cycle_complete

        val actionPauseTitle = context.getString(R.string.notification_action_timer_pause)
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(titleID))
                .setContentText(phaseCompleteMessage(phaseCompleted))
                .setContentIntent(phaseCompleteIntent())
                .addAction(android.R.drawable.ic_media_pause, actionPauseTitle, pauseTimerIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(
                        defaultFlagsForSettings(mapOf(
                                R.string.pref_key_notifications_sound_enabled to NotificationCompat.DEFAULT_SOUND,
                                R.string.pref_key_notifications_vibrate to NotificationCompat.DEFAULT_VIBRATE,
                                R.string.pref_key_notifications_led_enabled to NotificationCompat.DEFAULT_LIGHTS
                        ))
                )

        return builder.build()
    }

    /**
     * Check a SharedPreferences [Boolean] value to determine if a Notification flag should be set.
     *
     * @param prefKeyID Resource ID for the String key under which the preference is stored
     * @param prefFlag The flag to use if the preference corresponding to [prefKeyID] is set to true.
     *
     * @return [prefFlag] if the preference for [prefKeyID] is enabled, 0 otherwise.
     */
    private fun flagForSetting(@StringRes prefKeyID: Int, prefFlag: Int): Int =
            if (preferences.getBoolean(context.getString(prefKeyID)).get() ?: false) prefFlag else 0

    /**
     * Build Notification flags using values from SharedPreferences.
     *
     * @param keysToFlags A map from String resource IDs to the Notification flags that should be
     *                    set if the
     * @return Notification flags to be used with [NotificationCompat.Builder.setDefaults]
     */
    private fun defaultFlagsForSettings(keysToFlags: Map<Int, Int>): Int = keysToFlags.entries
                .map { flagForSetting(prefKeyID = it.key, prefFlag = it.value) }
                .fold(0) { combined, nextFlag -> combined or nextFlag }

    /**
     * Generate a content message to be displayed in a PHASE_COMPLETE notification.
     *
     * @param phase The phase that was completed.
     * @return The message to display in a notification.
     */
    private fun phaseCompleteMessage(phase: Cycle.Phase): CharSequence {
        if (phase == Cycle.Phase.WORK) {
            return context.getString(R.string.notification_message_work_cycle_complete)
        }

        val breakCycleMilliseconds = Cycle.Phase.WORK.defaultDuration * 1000
        val nextCycleTime = System.currentTimeMillis() + breakCycleMilliseconds
        val nextTime = DateUtils.getRelativeTimeSpanString(context, nextCycleTime, true)
        return context.getString(R.string.notification_message_break_cycle_complete, nextTime)
    }

    /**
     * Create an intent that returns the user to TimerActivity.
     * @return An intent to TimerActivity.
     */
    private fun phaseCompleteIntent(): PendingIntent {
        val timerIntent = Intent(context, TimerActivity::class.java)
        timerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        timerIntent.component = ComponentName(context, TimerActivity::class.java)
        return PendingIntent.getActivity(
                context,
                0,
                timerIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun pauseTimerIntent(): PendingIntent {
        val timerIntent = Intent(context, TimerActivity::class.java)
        timerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        timerIntent.component = ComponentName(context, TimerActivity::class.java)
        timerIntent.action = TimerContract.ACTION_PAUSE
        return PendingIntent.getActivity(
                context,
                0,
                timerIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    /**
     * Build and post a notification that a phase was completed.
     *
     * @param phase The phase that was completed.
     */
    fun notifyPhaseComplete(phase: Cycle.Phase) {
        Timber.v("Building cycle complete notification")
        val notification = phaseCompleteNotification(phase)
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notifyManager?.notify(ID_PHASE_COMPLETE, notification)
    }
}