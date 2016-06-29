package com.itsronald.twenty2020.settings

import com.itsronald.twenty2020.base.View


interface SettingsContract {

    interface SettingsView : View<Presenter> {
        fun refreshNightMode(nightMode: Int)
    }

    interface Presenter : com.itsronald.twenty2020.base.Presenter<SettingsView> {

    }
}