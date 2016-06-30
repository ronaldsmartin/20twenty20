package com.itsronald.twenty2020.settings

import com.itsronald.twenty2020.base.View


interface SettingsContract {

    interface SettingsView : View<Presenter> {
        /**
         * Notify the view that the global night mode has changed and that it may need to refresh
         * its layout.
         *
         * @param nightMode The new global night mode preference.
         */
        fun refreshNightMode(nightMode: Int)
    }

    interface Presenter : com.itsronald.twenty2020.base.Presenter<SettingsView> {

    }
}