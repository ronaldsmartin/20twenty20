package com.itsronald.twenty2020.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.StringRes
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.NotificationCompat
import android.text.format.DateUtils
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.timer.TimerActivity
import com.itsronald.twenty2020.timer.TimerContract
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.Random

/**
 * Used to build and post notifications to the system.
 */
class Notifier(val context: Context, val preferences: RxSharedPreferences) {

    companion object {
        /** ID for the notification for cycle phase completion */
        private val ID_PHASE_COMPLETE = 20

        /** ID for the persistent notification updated by ForegroundProgressService. */
        val ID_FOREGROUND_PROGRESS = 30
    }

    @Suppress("unused")
    private val foregroundNotificationSubscription = foregroundNotificationPref()
            .subscribe { enabled ->
                Timber.i(if (enabled) "Starting ForegroundProgressService" else "Ending ForegroundProgressService")

                val intent = Intent(context, ForegroundProgressService::class.java)
                if (enabled) context.startService(intent) else context.stopService(intent)
            }

    /***
     * Build a new notification indicating that the current phase is complete.
     *
     * @param phaseCompleted The phase that was completed.
     * @return a new notification for posting
     */
    private fun buildPhaseCompleteNotification(phaseCompleted: Cycle.Phase): Notification =
            NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(context, when(phaseCompleted) {
                    Cycle.Phase.WORK  -> R.color.solarized_red
                    Cycle.Phase.BREAK -> R.color.solarized_green
                }))
                .setContentTitle(context.getString(when(phaseCompleted) {
                    Cycle.Phase.WORK  -> R.string.notification_title_work_cycle_complete
                    Cycle.Phase.BREAK -> R.string.notification_title_break_cycle_complete
                }))
                .setContentText(makePhaseCompleteMessage(phaseCompleted))
                .setContentIntent(buildOpenTimerIntent())
                .addAction(
                        android.R.drawable.ic_media_pause,
                        context.getString(R.string.notification_action_timer_pause),
                        pauseTimerIntent()
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(
                        defaultFlagsForSettings(mapOf(
                                R.string.pref_key_notifications_vibrate to NotificationCompat.DEFAULT_VIBRATE,
                                R.string.pref_key_notifications_led_enabled to NotificationCompat.DEFAULT_LIGHTS
                        ))
                )
                .setSound(preferredNotificationSound)
                .build()

    /**
     * A URI for the notification sound chosen by the user in Settings.
     * If the user disabled sounds in the settings, this will be null.
     */
    private val preferredNotificationSound: Uri?
        get() {
            val soundPreferenceKey = context.getString(R.string.pref_key_notifications_sound_enabled)
            val soundEnabled = preferences.getBoolean(soundPreferenceKey).get() ?: false
            return if (soundEnabled) preferences
                    .getString(context.getString(R.string.pref_key_notifications_ringtone))
                    .get()?.let { Uri.parse(it) }
                   else null
        }

    /**
     * Check a SharedPreferences [Boolean] value to determine if a Notification flag should be set.

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

    /** RNG used for selecting strings in [makePhaseCompleteMessage]. */
    private val random = Random()

    /**
     * Generate a content message to be displayed in a PHASE_COMPLETE notification.
     *
     * @param phase The phase that was completed.
     * @return The message to display in a notification.
     */
    private fun makePhaseCompleteMessage(phase: Cycle.Phase): CharSequence = when(phase) {
        Cycle.Phase.WORK  -> {
            // Choose a random exercise to suggest for the break.
            val messages = context.resources
                    .getStringArray(R.array.notification_messages_work_cycle_complete)
            messages[random.nextInt(messages.size)]
        }
        Cycle.Phase.BREAK -> {
            // Notify the user what time the next break will occur.
            val breakCycleMilliseconds = Cycle.Phase.WORK.defaultDuration * 1000
            val nextCycleTime = System.currentTimeMillis() + breakCycleMilliseconds
            val nextTime = DateUtils.getRelativeTimeSpanString(context, nextCycleTime, true)
            context.getString(R.string.notification_message_break_cycle_complete, nextTime)
        }
    }

    /**
     * Create an intent that returns the user to TimerActivity.
     * @return An intent to TimerActivity.
     */
    private fun buildOpenTimerIntent(): PendingIntent {
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

    /**
     * Build an intent that pauses the cycle timer.
     */
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
        Timber.v("Building cycle complete notification.")
        val notification = buildPhaseCompleteNotification(phase)
        Timber.i("Posting cycle complete notification.")
        val notifyManager = NotificationManagerCompat.from(context)
        notifyManager.notify(ID_PHASE_COMPLETE, notification)
    }

    //region Foreground progress notification

    private fun foregroundNotificationPref(): Observable<Boolean> = preferences
            .getBoolean(context.getString(R.string.pref_key_notifications_persistent_enabled))
            .asObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext {
                Timber.e(it, "Encountered an error while watching foreground notification preference.")
                foregroundNotificationPref()
            }

    /**
     * Create a notification displaying a cycle's current progress.
     *
     * See also: [notifyUpdatedProgress]
     *
     * @param cycle The cycle whose progress should be displayed in the notification.
     * @return A new notification displaying the progress of [cycle].
     */
    fun buildProgressNotification(cycle: Cycle): Notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title_foreground_progress))
            .setContentText(progressNotificationMessage(cycle))
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setContentIntent(buildOpenTimerIntent())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(cycle.running)
            .setProgress(cycle.duration, cycle.elapsedTime, false)
            .build()

    /**
     * Generate the message to be used in the foreground progress notification.
     *
     * See: [buildProgressNotification].
     *
     * @param cycle The cycle whose progress will be displayed in the notification.
     * @return The content title for the [Cycle] progress notification.
     */
    private fun progressNotificationMessage(cycle: Cycle): String =
        if (cycle.running) context.getString(
                R.string.notification_message_foreground_progress,
                cycle.phaseName
        ) else context.getString(
                R.string.notification_message_foreground_progress_paused,
                cycle.phaseName
        )

    /**
     * Build and post a notification of the cycle's current progress.
     *
     * See also: [buildProgressNotification]
     *
     * @param cycle The cycle whose progress should be displayed in the notification.
     */
    fun notifyUpdatedProgress(cycle: Cycle) {
        Timber.v("Posting foreground progress notification for cycle: $cycle")
        val notification = buildProgressNotification(cycle)
        val notifyManager = NotificationManagerCompat.from(context)
        notifyManager.notify(ID_FOREGROUND_PROGRESS, notification)
    }

    //endregion
}