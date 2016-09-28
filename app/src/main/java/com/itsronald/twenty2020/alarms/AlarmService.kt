package com.itsronald.twenty2020.alarms

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.reporting.EventTracker
import timber.log.Timber


class AlarmService() : IntentService(SERVICE_NAME) {

    private companion object {
        const val SERVICE_NAME = "com.itsronald.twenty2020.alarms.alarm_service"
    }

    private val shouldAutoStartNextPhase: Boolean
        get() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val prefKey = getString(R.string.pref_key_general_auto_start_next_phase)
            return prefs.getBoolean(prefKey, true)
        }

    override fun onHandleIntent(intent: Intent) {
        Timber.i("Received intent: $intent")
        val appComponent = (application as Twenty2020Application).appComponent

        // While enums are Serializable and can be passed directly through an intent, it is
        // possible for a ClassNotFoundException to occur while de-serializing the extra.
        // Passing the name of the phase instead is a suitable workaround.
        // See http://stackoverflow.com/q/2307476/4499783 for more details.
        val completedPhaseName = intent.getStringExtra(AlarmScheduler.EXTRA_PHASE)
        val completedPhase = Cycle.Phase.valueOf(completedPhaseName)
        Timber.i("Notifying user for completion of phase: $completedPhaseName")
        appComponent.notifier().notifyPhaseComplete(completedPhase)

        val cycle = appComponent.cycle()
        resyncCycle(cycle = cycle, completedPhase = completedPhase)

        Timber.i("Updating alarms.")
        appComponent.alarmScheduler().updateAlarms()

        val newEvent = EventTracker.Event.PhaseCompleted(cycle = cycle, phase = completedPhase)
        appComponent.eventTracker().reportEvent(newEvent)

        val completedWakelock = AlarmReceiver.completeWakefulIntent(intent)
        Timber.v("Completed wakeful intent: $completedWakelock")

        Timber.v("Finished operation.")
    }

    /**
     * If the application process was killed, we'll want to use this scheduled alarm to restart
     * the app Cycle.
     */
    private fun resyncCycle(cycle: Cycle, completedPhase: Cycle.Phase) {
        if (cycle.phase == completedPhase) {
            Timber.i("Cycle is out of sync. Starting next phase.")
            cycle.startNextPhase()
        }

        if (shouldAutoStartNextPhase) {
            Timber.i("Cycle was killed. Restarting cycle.")
            cycle.start()
        } else {
            Timber.v("Cycle is not permitted to auto-start. Pausing for next phase.")
            cycle.pause()
        }
    }
}