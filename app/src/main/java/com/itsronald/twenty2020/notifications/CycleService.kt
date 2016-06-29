package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.settings.DaggerSettingsComponent
import com.itsronald.twenty2020.settings.SettingsModule
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class CycleService : Service() {

    @Inject
    lateinit var cycle: Cycle

    private lateinit var notificationsUpdater: Subscription

    private val notifier = NotificationHelper(this)

    override fun onCreate() {
        super.onCreate()
        (application as? Twenty2020Application)?.cycleComponent?.inject(this)
        notificationsUpdater = cycle.timer
                .filter { it.elapsedTime == it.duration - 1 }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.v("Dispatching phase complete notification for phase ${it.phase}.")
                    notifier.notifyPhaseComplete(it.phase)
                }
        DaggerSettingsComponent.builder()
                .settingsModule(SettingsModule(this))
                .build()
                .inject(notifier)
        Timber.v("Service created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationsUpdater.unsubscribe()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("Starting service.")
        return START_STICKY
    }
}