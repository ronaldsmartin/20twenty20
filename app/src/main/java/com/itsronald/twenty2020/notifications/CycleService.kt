package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.settings.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.PreferencesModule
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * A CycleService performs the application's main task of notifying the user of the running cycle's
 * progress and phase completion.
 */
class CycleService : Service() {

    /** The state of the repeating timer. */
    @Inject
    lateinit var cycle: Cycle

    /** Object responsible for building and display notifications of Cycle events. */
    private val notifier = NotificationHelper(this)

    /** Subscriptions  */
    private val subscriptions = CompositeSubscription()

    //region Service lifecycle

    override fun onCreate() {
        super.onCreate()
        (application as? Twenty2020Application)?.cycleComponent?.inject(this)
        DaggerPreferencesComponent.builder()
                .preferencesModule(PreferencesModule(this))
                .build()
                .inject(notifier)

        subscriptions.add(notifyUserPhaseCompleted.subscribe())
        subscriptions.add(notifier.foregroundNotePreference.subscribe { foregroundEnabled ->
            if (foregroundEnabled)
                startForeground(NotificationHelper.ID_FOREGROUND_PROGRESS,
                                notifier.progressNotification(cycle))
            else stopForeground(true)
        })
        subscriptions.add(updateForegroundProgress.subscribe())
        Timber.v("Service created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.unsubscribe()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("Starting service.")
        return START_STICKY
    }

    //endregion

    private val notifyUserPhaseCompleted: Observable<Cycle>
        get() = cycle.timer
                .filter { it.elapsedTime == it.duration - 1 }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Timber.e(it, "Unable to notify user of phase completion!") }
                .doOnNext {
                    Timber.v("Dispatching phase complete notification for phase ${it.phase}.")
                    notifier.notifyPhaseComplete(it.phase)
                }

    private val updateForegroundProgress: Observable<Cycle>
        get() = cycle.timer
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Timber.e(it, "Unable to notify user of cycle progress") }
                .doOnNext {
                    Timber.v("Updating foreground cycle progress notification.")
                    notifier.notifyUpdatedProgress(it)
                }

}