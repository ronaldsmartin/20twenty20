package com.itsronald.twenty2020.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsronald.twenty2020.timer.TimerActivity
import com.itsronald.twenty2020.timer.TimerContract
import timber.log.Timber


/**
 * Handles actions added to notifications through the Notification.addAction API.
 *
 * This receiver is responsible for cancelling notifications after their actions have been clicked.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received broadcast for action: ${intent.action}")
        handleNotificationAction(intent.action, context = context)

        val shouldCancelSourceNotification = intent
                .getBooleanExtra(Notifier.EXTRA_FLAG_CANCEL_NOTIFICATION, false)
        if (shouldCancelSourceNotification) {
            val notificationID = intent.getIntExtra(Notifier.EXTRA_NOTIFICATION_ID, -1)
            cancelSourceNotification(context = context, notificationID = notificationID)
        } else {
            Timber.v("FLAG_CANCEL_NOTIFICATION was not set. Notification will not be cancelled.")
        }
    }

    private fun handleNotificationAction(action: String, context: Context) {
        Timber.v("Handling notification action $action.")
        when (action) {
            Notifier.ACTION_PAUSE_TIMER -> context.startActivity(
                    timerActivityIntent(context = context).setAction(TimerContract.ACTION_PAUSE)
            )
            // Additional notification action handlers can be added here.
            else -> {
                val exception = IllegalArgumentException("Unknown notification action name: $action")
                Timber.e(exception, "Unable to handle unknown notification action")
                throw exception
            }
        }
    }

    private fun timerActivityIntent(context: Context): Intent = TimerActivity
            .intent(context = context)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun cancelSourceNotification(context: Context, notificationID: Int) {
        if (notificationID == -1) {
            Timber.w("Notification ID was not passed with Intent! Unable to cancel notification.")
            return
        }

        Timber.i("Cancelling notification with ID: $notificationID.")
        val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)
    }
}