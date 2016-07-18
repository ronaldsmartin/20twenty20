package com.itsronald.twenty2020.settings

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceRepository
import dagger.Module
import dagger.Provides


@Module
class SettingsModule(private val view: SettingsContract.SettingsView) {

    @Provides
    @Activity
    fun provideSettingsView(): SettingsContract.SettingsView = view

    @Provides
    @Activity
    fun providePresenter(resources: ResourceRepository,
                         preferences: RxSharedPreferences): SettingsContract.Presenter =
            SettingsPresenter(view, resources, preferences)
}