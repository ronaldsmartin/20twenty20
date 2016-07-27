package com.itsronald.twenty2020.alarms

import android.app.AlarmManager
import android.content.Context
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
        const val REQUEST_CODE_NOTIFY_PHASE_COMPLETE = 10
    }

    //region lifecycle

    fun onCreate() {
        Timber.i("Scheduler created.")
    }

    fun onDestroy() {
        Timber.i("Scheduler destroyed.")
    }

    //endregion

}