package com.itsronald.twenty2020.settings

import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceComponent
import dagger.Component

@Activity
@Component(
        dependencies = arrayOf(PreferencesComponent::class, ResourceComponent::class),
        modules = arrayOf(SettingsModule::class)
)
interface SettingsComponent {
    fun inject(settingsActivity: SettingsActivity)
}