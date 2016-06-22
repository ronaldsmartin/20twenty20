package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.notifications.CycleService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(CycleModule::class))
interface CycleComponent {
    fun cycle(): Cycle
    fun inject(cycleService: CycleService)
}