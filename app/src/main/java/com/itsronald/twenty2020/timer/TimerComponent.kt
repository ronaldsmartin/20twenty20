package com.itsronald.twenty2020.timer

import dagger.Component

@Component(
        modules = arrayOf(TimerModule::class)
)
interface TimerComponent {

    fun inject(timerActivity: TimerActivity)
}