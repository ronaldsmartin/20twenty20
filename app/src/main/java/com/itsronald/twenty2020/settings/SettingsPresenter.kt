package com.itsronald.twenty2020.settings

import android.support.v7.app.AppCompatDelegate
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject


class SettingsPresenter
    @Inject constructor(override var view: SettingsContract.SettingsView,
                        val preferences: RxSharedPreferences)
    : SettingsContract.Presenter {

    private lateinit var subscriptions: CompositeSubscription

    val nightModeObserver = preferences
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
            .doOnError { Timber.e(it, "Unable to observer night mode.") }
            .doOnNext {
                val nightModeName = when (it) {
                    AppCompatDelegate.MODE_NIGHT_AUTO -> "MODE_NIGHT_AUTO"
                    AppCompatDelegate.MODE_NIGHT_NO   -> "MODE_NIGHT_NO"
                    AppCompatDelegate.MODE_NIGHT_YES  -> "MODE_NIGHT_YES"
                    else                              -> "UNKNOWN"
                }
                Timber.v("Night mode changed to $nightModeName")
                AppCompatDelegate.setDefaultNightMode(it)
                view.refreshNightMode(it)
            }

    override fun onStart() {
        super.onStart()
        Timber.v("SettingsPresenter created.")

        subscriptions = CompositeSubscription()
        subscriptions.add(nightModeObserver.subscribe())
    }

    override fun onStop() {
        super.onStop()
        Timber.v("SettingsPresenter is stopping.")
        subscriptions.unsubscribe()
    }
}