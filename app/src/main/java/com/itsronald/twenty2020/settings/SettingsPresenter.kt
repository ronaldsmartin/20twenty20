package com.itsronald.twenty2020.settings

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatDelegate
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
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

    /**
     * Observe changes to the display_location_based_night_mode SharedPreference.
     * The original value (the current setting when the Observer is started) is not sent to
     * subscribers.
     *
     * @return A new Observable that reacts to changes to the display_location_based_night_mode
     * setting.
     */
    private fun watchNightModeLocationPreference(): Observable<Boolean> = preferences
            .getBoolean(view.context.getString(R.string.pref_key_display_location_based_night_mode))
            .asObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onError { Timber.e(it, "Unable to observe night mode location setting.") }

    //endregion

    //region Activity lifecycle

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Dexter.continuePendingRequestIfPossible(buildDexterPermissionDeniedListener())
    }

    override fun onStart() {
        super.onStart()
        Timber.v("SettingsPresenter created.")

        startSubscriptions()
    }

    override fun onStop() {
        super.onStop()
        Timber.v("SettingsPresenter is stopping.")
        subscriptions.unsubscribe()
    }

    //endregion

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

        subscriptions.add(watchNightModeLocationPreference().subscribe { enabled ->
            if (enabled) ensureLocationPermission()
        })
    }

    /**
     * If a location permissions request is not already in progress, request the
     * ACCESS_COARSE_LOCATION permission from the user.
     *
     * If the request is denied, display a Snackbar via Dexter.
     */
    private fun ensureLocationPermission() {
        if (Dexter.isRequestOngoing()) {
            Timber.v("Permissions request is already occurring. Skipping duplicate request.")
            return
        }

        Timber.v("Requesting permission ${Manifest.permission.ACCESS_COARSE_LOCATION}.")
        Dexter.checkPermission(buildDexterPermissionDeniedListener(),
                Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Create a PermissionListener to be used by Dexter for notifying the user when permissions
     * are denied.
     */
    private fun buildDexterPermissionDeniedListener(): PermissionListener =
            SnackbarOnDeniedPermissionListener.Builder
                    .with(view.contentView, R.string.location_permission_rationale)
                    .withOpenSettingsButton(R.string.settings)
                    .withCallback(object : Snackbar.Callback() {
                        override fun onShown(snackbar: Snackbar?) {
                            super.onShown(snackbar)
                            // If the Snackbar is shown, the permission was denied.
                            // Un-check the setting that requires the permission.
                            Timber.w("Permission request was denied. Disabling automatic night mode.")
                            view.setPreferenceChecked(
                                    prefKeyID = R.string.pref_key_display_location_based_night_mode,
                                    checked = false
                            )
                        }
                    })
                    .build()

}