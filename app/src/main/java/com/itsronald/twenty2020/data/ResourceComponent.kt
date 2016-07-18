package com.itsronald.twenty2020.data

import dagger.Component

@Component(modules = arrayOf(ResourceModule::class))
interface ResourceComponent {
    fun resources(): ResourceRepository
}