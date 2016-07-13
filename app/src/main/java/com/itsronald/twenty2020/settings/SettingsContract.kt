package com.itsronald.twenty2020.settings

import android.support.annotation.StringRes
import android.view.ViewGroup
import com.itsronald.twenty2020.base.View


interface SettingsContract {

    interface SettingsView : View<Presenter> {

        /** The view used by Dexter to display permission denied warnings. */
        val contentView: ViewGroup

        /**
         * Notify the view that the global night mode has changed and that it may need to refresh
         * its layout.
         *
         * @param nightMode The new global night mode preference.
         */
        fun refreshNightMode(nightMode: Int)

        /**
         * Attempt to set the switch value of a two-state preference with key accessible
         * via [prefKeyID].
         *
         * @param prefKeyID The resource ID of the String key of the Preference to modify.
         * @param checked The value to set for the preference's state.
         *
         * @return true if [prefKeyID] led to a valid two-state preference whose value was set to
         * [checked]; false otherwise.
         */
        fun setPreferenceChecked(@StringRes prefKeyID: Int, checked: Boolean): Boolean

        /**
         * Remove a preference item with key [prefKeyID] from the view.
         *
         * @param prefKeyID The resource ID of the String key of the Preference to hide.
         *
         * @return Whether or not the preference was found and removed.
         */
        fun removePreference(@StringRes prefKeyID: Int): Boolean
    }

    interface Presenter : com.itsronald.twenty2020.base.Presenter<SettingsView> {

    }
}