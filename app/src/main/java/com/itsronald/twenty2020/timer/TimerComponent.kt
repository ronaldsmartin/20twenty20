package com.itsronald.twenty2020.timer

import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.model.CycleComponent
import dagger.Component

@Activity
@Component(
        dependencies = arrayOf(CycleComponent::class),
        modules = arrayOf(TimerModule::class)
)
interface TimerComponent {

    fun inject(timerActivity: TimerActivity)
}