package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.data.ResourceComponent
import com.itsronald.twenty2020.notifications.CycleService
import com.itsronald.twenty2020.settings.PreferencesComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        dependencies = arrayOf(ResourceComponent::class),
        modules = arrayOf(CycleModule::class)
)
interface CycleComponent {
    fun cycle(): Cycle
    fun inject(cycleService: CycleService)
}