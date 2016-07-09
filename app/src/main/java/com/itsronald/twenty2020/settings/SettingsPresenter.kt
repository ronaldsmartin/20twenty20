package com.itsronald.twenty2020.settings

import android.support.v7.app.AppCompatDelegate
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onError
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject


class SettingsPresenter
    @Inject constructor(override var view: SettingsContract.SettingsView,
                        val preferences: RxSharedPreferences)
    : SettingsContract.Presenter {

    //region Observers

    /** Subscriptions maintained by this presenter. */
    private lateinit var subscriptions: CompositeSubscription

    /**
     * Observe changes to the display_night_mode SharedPreference.
     * The original value (the current setting when the Observer is started) is not sent to
     * subscribers.
     *
     * @return A new Observable that reacts to changes to the display_night_mode setting.
     */
    private fun watchNightModePreference(): Observable<Int> = preferences
            .getString(view.context.getString(R.string.pref_key_display_night_mode))
            .asObservable()
            .skip(1) // Skip the initial lookup (preference has not been changed).
            .map { it.toInt() }
            .filter {
                AppCompatDelegate.getDefaultNightMode() != it
                        && (it == AppCompatDelegate.MODE_NIGHT_NO
                        ||  it == AppCompatDelegate.MODE_NIGHT_YES
                        ||  it == AppCompatDelegate.MODE_NIGHT_AUTO)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to observe night mode setting.") }

    //endregion

    override fun onStart() {
        super.onStart()
        Timber.v("SettingsPresenter created.")

        startSubscriptions()
    }

    private fun startSubscriptions() {
        subscriptions = CompositeSubscription()
        subscriptions.add(watchNightModePreference().subscribe {
            val nightModeName = when (it) {
                AppCompatDelegate.MODE_NIGHT_AUTO -> "MODE_NIGHT_AUTO"
                AppCompatDelegate.MODE_NIGHT_NO   -> "MODE_NIGHT_NO"
                AppCompatDelegate.MODE_NIGHT_YES  -> "MODE_NIGHT_YES"
                else                              -> "UNKNOWN"
            }
            Timber.v("Night mode changed to $nightModeName")
            AppCompatDelegate.setDefaultNightMode(it)
            view.refreshNightMode(it)
        })
    }

    override fun onStop() {
        super.onStop()
        Timber.v("SettingsPresenter is stopping.")
        subscriptions.unsubscribe()
    }
}