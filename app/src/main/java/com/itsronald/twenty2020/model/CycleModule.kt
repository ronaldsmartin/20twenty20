package com.itsronald.twenty2020.model

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.data.ResourceRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides the singleton Cycle to components that require it.
 */
@Module
class CycleModule() {

    @Provides
    @Singleton
    fun provideCycle(resourceRepository: ResourceRepository,
                     preferences: RxSharedPreferences): Cycle =
            Cycle(resourceRepository, preferences)
}