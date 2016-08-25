package com.itsronald.twenty2020.alarms

import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver
import timber.log.Timber

/**
 * Receives and processes scheduled alarms from [AlarmScheduler].
 *
 * @see AlarmScheduler
 */
class AlarmReceiver : WakefulBroadcastReceiver() {

    companion object {
        const val ACTION_NOTIFY = "com.itsronald.twenty2020.action.alarm.notify"

        fun completeWakefulIntent(intent: Intent): Boolean =
                WakefulBroadcastReceiver.completeWakefulIntent(intent)
    }

    init {
        Timber.i("Receiver created.")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received broadcast: $intent")

        Timber.i("Passing broadcast to AlarmService.")
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.putExtras(intent)
        startWakefulService(context, serviceIntent)

        Timber.i("Finished broadcast: $intent")
    }

}