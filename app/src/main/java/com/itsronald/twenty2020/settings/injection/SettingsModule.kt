package com.itsronald.twenty2020.settings.injection

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.data.ResourceRepository
import com.itsronald.twenty2020.settings.SettingsContract
import com.itsronald.twenty2020.settings.SettingsPresenter
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