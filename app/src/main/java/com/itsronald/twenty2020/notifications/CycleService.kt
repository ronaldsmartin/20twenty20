package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.settings.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.PreferencesModule
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.filterNotNull
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

        Timber.v("Service created.")
        startSubscriptions()
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

    private fun startSubscriptions() {
        Timber.v("Starting subscriptions.")
        subscriptions.add(watchPhaseCompletion().subscribe {
            notifier.notifyPhaseComplete(it.phase)
        })

        subscriptions.add(watchForegroundNotificationPref().subscribe { foregroundEnabled ->
            if (foregroundEnabled)
                startForeground(NotificationHelper.ID_FOREGROUND_PROGRESS,
                        notifier.buildProgressNotification(cycle))
            else stopForeground(true)
        })

        subscriptions.add(updateForegroundProgress().subscribe {
            notifier.notifyUpdatedProgress(it)
        })
    }

    /**
     * Observe events when the current Cycle phase completes.
     */
    private fun watchPhaseCompletion(): Observable<Cycle> = cycle.timer
                .filter { it.isFinishingPhase }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Timber.e(it, "Unable to notify user of phase completion!") }
                .doOnNext {
                    Timber.v("Dispatching phase complete notification for phase ${it.phase}.")
                }

    /**
     * Watch the user preference for notifications_persistent_enabled, signaling when it changes.
     */
    private fun watchForegroundNotificationPref(): Observable<Boolean> = notifier
            .foregroundNotificationPref()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Timber.e(it, "Unable to watch foreground notification preference") }
            .doOnNext { foregroundEnabled ->
                if (foregroundEnabled)
                    Timber.i("Starting foreground progress notification.")
                else Timber.i("Stopping foreground progress notification.")
            }

    /**
     * When the user preference notifications_persistent_enabled is enabled, signal with the current
     * Cycle progress.
     */
    private fun updateForegroundProgress(): Observable<Cycle> = cycle.timer
                .withLatestFrom(watchForegroundNotificationPref()) { cycle, foregroundNoteEnabled ->
                    if (foregroundNoteEnabled) cycle else null
                }
                .filterNotNull()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Timber.e(it, "Unable to notify user of cycle progress") }
                .doOnNext {
                    Timber.v("Updating foreground cycle progress notification.")
                }

}