package com.itsronald.twenty2020.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.reporting.EventTracker
import timber.log.Timber

/**
 * Receives and processes scheduled alarms from [AlarmScheduler].
 *
 * @see AlarmScheduler
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_NOTIFY = "com.itsronald.twenty2020.action.alarm.notify"
    }

    init {
        Timber.i("Receiver created.")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received broadcast: $intent")

        val appComponent = Twenty2020Application.INSTANCE.appComponent

        // While enums are Serializable and can be passed directly through an intent, it is
        // possible for a ClassNotFoundException to occur while de-serializing the extra.
        // Passing the name of the phase instead is a suitable workaround.
        // See http://stackoverflow.com/q/2307476/4499783 for more details.
        val completedPhaseName = intent.getStringExtra(AlarmScheduler.EXTRA_PHASE)
        val completedPhase = Cycle.Phase.valueOf(completedPhaseName)
        appComponent.notifier().notifyPhaseComplete(completedPhase)

        val cycle = appComponent.cycle()

        if (!cycle.running) {
            if (cycle.phase == completedPhase) {
                Timber.i("Cycle is out of sync. Starting next phase.")
                cycle.startNextPhase()
            }
            Timber.i("Cycle was killed. Restarting cycle.")
            cycle.start()
        }

        Timber.i("Updating alarms.")
        appComponent.alarmScheduler().updateAlarms()

        val newEvent = EventTracker.Event.PhaseCompleted(cycle = cycle, phase = completedPhase)
        appComponent.eventTracker().reportEvent(newEvent)
    }

}