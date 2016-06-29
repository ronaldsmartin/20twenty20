package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.notifications.CycleService
import com.itsronald.twenty2020.settings.SettingsComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        dependencies = arrayOf(SettingsComponent::class),
        modules = arrayOf(CycleModule::class)
)
interface CycleComponent {
    fun cycle(): Cycle
    fun inject(cycleService: CycleService)
}