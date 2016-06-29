package com.itsronald.twenty2020.settings

import com.f2prateek.rx.preferences.RxSharedPreferences
import javax.inject.Inject


class SettingsPresenter
    @Inject constructor(override var view: SettingsContract.SettingsView,
                        val preferences: RxSharedPreferences)
    : SettingsContract.Presenter {
}