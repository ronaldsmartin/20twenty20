package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * A service that provides the user with a progress notification in the notification shade.
 * This is enabled in the user settings.
 */
class CycleService : Service() {

    /** The state of the repeating timer. */
    @Inject
    lateinit var cycle: Cycle

    /** Object responsible for building and display notifications of Cycle events. */
    @Inject
    lateinit var notifier: Notifier

    /** Subscriptions */
    private val subscriptions = CompositeSubscription()

    //region Service lifecycle

    override fun onCreate() {
        super.onCreate()
        (application as? Twenty2020Application)?.appComponent?.inject(this)

        Timber.v("Service created.")
        startSubscriptions()
    }

    override fun onDestroy() {
        Timber.i("Stopping foreground progress notification.")
        stopForeground(true)

        subscriptions.unsubscribe()
        Timber.d("Service destroyed.")

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("Starting service: $intent; flags: $flags; startID: $startId")
        startForeground(Notifier.ID_FOREGROUND_PROGRESS, notifier.buildProgressNotification(cycle))
        return START_NOT_STICKY
    }

    //endregion

    private fun startSubscriptions() {
        Timber.v("Starting subscriptions.")

        subscriptions += cycleTimerTicks().subscribe {
            notifier.notifyUpdatedProgress(it)
        }
    }

    private fun cycleTimerTicks(): Observable<Cycle> = cycle.timer
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext {
                Timber.e(it, "Unable to notify user of cycle progress")
                cycleTimerTicks()
            }
            .doOnNext {
                Timber.v("Updating foreground cycle progress notification.")
            }

}