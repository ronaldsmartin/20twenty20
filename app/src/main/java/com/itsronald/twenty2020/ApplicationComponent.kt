package com.itsronald.twenty2020

import com.itsronald.twenty2020.data.ResourceComponent
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.CycleModule
import com.itsronald.twenty2020.notifications.CycleService
import com.itsronald.twenty2020.notifications.NotificationModule
import com.itsronald.twenty2020.notifications.Notifier
import com.itsronald.twenty2020.settings.PreferencesComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        dependencies = arrayOf(ResourceComponent::class, PreferencesComponent::class),
        modules = arrayOf(CycleModule::class, NotificationModule::class)
)
interface ApplicationComponent {
    fun cycle(): Cycle
    fun notifier(): Notifier
    fun inject(cycleService: CycleService)
}