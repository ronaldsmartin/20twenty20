package com.itsronald.twenty2020.settings.injection

import android.support.design.widget.Snackbar
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceRepository
import com.itsronald.twenty2020.settings.SettingsContract
import com.itsronald.twenty2020.settings.SettingsPresenter
import com.itsronald.twenty2020.settings.SnackbarPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import dagger.Module
import dagger.Provides
import timber.log.Timber


@Module
class SettingsModule(private val view: SettingsContract.SettingsView) {

    @Provides @Activity
    fun provideSettingsView(): SettingsContract.SettingsView = view

    @Provides @Activity
    fun providePresenter(resources: ResourceRepository,
                         preferences: RxSharedPreferences): SettingsContract.Presenter =
            SettingsPresenter(view, resources, preferences)

    @Provides @Activity
    fun provideSnackbarOnDeniedPermissionsListener(callback: Snackbar.Callback)
            : SnackbarOnDeniedPermissionListener =
            SnackbarOnDeniedPermissionListener.Builder
                    .with(view.contentView, R.string.location_permission_rationale)
                    .withOpenSettingsButton(R.string.settings)
                    .withCallback(callback)
                    .build()

    @Provides @Activity
    fun providePermissionsDeniedCallback(): Snackbar.Callback = object : Snackbar.Callback() {
        override fun onShown(snackbar: Snackbar) {
            super.onShown(snackbar)
            // If the Snackbar is shown, the permission was denied.
            // Un-check the setting that requires the permission.
            Timber.w("Permission request was denied. Disabling automatic night mode.")
            view.setPreferenceChecked(
                    prefKeyID = R.string.pref_key_display_location_based_night_mode,
                    checked = false
            )
        }
    }

    @Provides @Activity
    fun providePermissionsListener(listener: SnackbarPermissionListener): PermissionListener = listener
}