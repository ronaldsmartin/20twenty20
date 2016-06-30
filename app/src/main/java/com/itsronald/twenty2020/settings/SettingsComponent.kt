package com.itsronald.twenty2020.settings

import com.itsronald.twenty2020.base.Activity
import dagger.Component

@Activity
@Component(
        dependencies = arrayOf(PreferencesComponent::class),
        modules = arrayOf(SettingsModule::class)
)
interface SettingsComponent {
    fun inject(settingsActivity: SettingsActivity)
}