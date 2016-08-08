package com.itsronald.twenty2020

import com.itsronald.twenty2020.alarms.AlarmModule
import com.itsronald.twenty2020.alarms.AlarmScheduler
import com.itsronald.twenty2020.data.ResourceComponent
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.CycleModule
import com.itsronald.twenty2020.notifications.ForegroundProgressService
import com.itsronald.twenty2020.notifications.NotificationModule
import com.itsronald.twenty2020.notifications.Notifier
import com.itsronald.twenty2020.settings.PreferencesComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        dependencies = arrayOf(ResourceComponent::class, PreferencesComponent::class),
        modules = arrayOf(AlarmModule::class, CycleModule::class, NotificationModule::class)
)
interface ApplicationComponent {
    fun cycle(): Cycle
    fun notifier(): Notifier
    fun alarmScheduler(): AlarmScheduler
    fun inject(foregroundProgressService: ForegroundProgressService)
}