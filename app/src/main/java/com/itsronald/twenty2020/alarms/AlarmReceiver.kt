package com.itsronald.twenty2020.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import timber.log.Timber

/**
 * Receives and processes scheduled alarms from [AlarmScheduler].
 *
 * @see AlarmScheduler
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received broadcast: $intent")

        val appComponent = Twenty2020Application.INSTANCE.appComponent

        val completedPhase = intent.getSerializableExtra(AlarmScheduler.EXTRA_PHASE) as Cycle.Phase
        appComponent.notifier().notifyPhaseComplete(completedPhase)

        val cycle = appComponent.cycle()
        if (cycle.phase == completedPhase) {
            Timber.i("Cycle is out of sync. Starting next phase.")
            cycle.startNextPhase()
            cycle.start()
        }

        Timber.i("Updating alarms.")
        appComponent.alarmScheduler().updateAlarms()
    }

}