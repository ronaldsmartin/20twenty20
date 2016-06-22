package com.itsronald.twenty2020.model

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(CycleModule::class))
interface CycleComponent {
}