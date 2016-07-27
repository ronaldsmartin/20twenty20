package com.itsronald.twenty2020.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Receives scheduled alarm intents from AlarmScheduler.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Implement
        Timber.e("Unimplemented")
        throw UnsupportedOperationException("Unimplemented")
    }

}