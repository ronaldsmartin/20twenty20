package com.itsronald.twenty2020.settings.injection

import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceComponent
import com.itsronald.twenty2020.settings.SettingsActivity
import com.karumi.dexter.listener.single.PermissionListener
import dagger.Component

@Activity
@Component(
        dependencies = arrayOf(PreferencesComponent::class, ResourceComponent::class),
        modules = arrayOf(SettingsModule::class)
)
interface SettingsComponent {
    fun permissionsListener(): PermissionListener
    fun inject(settingsActivity: SettingsActivity)
}