package com.itsronald.twenty2020.timer

import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceComponent
import com.itsronald.twenty2020.model.CycleComponent
import com.itsronald.twenty2020.settings.PreferencesComponent
import dagger.Component

@Activity
@Component(
        dependencies = arrayOf(
                CycleComponent::class, PreferencesComponent::class, ResourceComponent::class
        ),
        modules = arrayOf(TimerModule::class)
)
interface TimerComponent {

    fun inject(timerActivity: TimerActivity)
}