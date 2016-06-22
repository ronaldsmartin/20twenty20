package com.itsronald.twenty2020.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.NotificationCompat
import android.text.format.DateUtils
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.timer.TimerActivity
import com.itsronald.twenty2020.timer.TimerContract
import timber.log.Timber


class NotificationHelper(private val context: Context) {

    companion object {
        private val ID_PHASE_COMPLETE = 20
    }

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
        Intent.ACTION_SEARCH
        val actionPauseTitle = context.getString(R.string.notification_action_timer_pause)
        return NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(titleID))
                .setContentText(phaseCompleteMessage(phaseCompleted))
                .setContentIntent(phaseCompleteIntent())
                .addAction(android.R.drawable.ic_media_pause, actionPauseTitle, pauseTimerIntent())
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLights(Color.WHITE, 1000, 100)
                .build()
    }

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
        timerIntent.setAction(TimerContract.ACTION_PAUSE)
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
     * @param phaseCompleted The phase that was completed.
     */
    fun notifyPhaseComplete(phase: Cycle.Phase) {
        Timber.v("Building cycle complete notification")
        val notification = phaseCompleteNotification(phase)
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notifyManager?.notify(ID_PHASE_COMPLETE, notification)
    }
}