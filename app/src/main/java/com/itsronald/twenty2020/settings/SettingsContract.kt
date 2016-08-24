package com.itsronald.twenty2020.settings

import android.support.annotation.StringRes
import android.view.ViewGroup
import com.itsronald.twenty2020.base.View
import com.itsronald.twenty2020.settings.injection.SettingsComponent


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
         * Find and disable user interaction with the Preference item associated with a given key.
         *
         * @param prefKeyID The resource ID of the String key of the Preference to modify.
         * @param enabled Whether to enable or disable the Preference associated with [prefKeyID].
         *
         * @return Whether or not the preference was found and modified.
         */
        fun setPreferenceEnabled(@StringRes prefKeyID: Int, enabled: Boolean): Boolean

        /**
         * Remove a preference item with key [prefKeyID] from the view.
         *
         * @param prefKeyID The resource ID of the String key of the Preference to hide.
         * @param inCategory The resource ID of the String key of the PreferenceCategory containing
         * the preference, if it is enclosed in a category.
         *
         * @return Whether or not the preference was found and removed.
         */
        fun removePreference(@StringRes prefKeyID: Int, @StringRes inCategory: Int? = null): Boolean
    }

    interface Presenter : com.itsronald.twenty2020.base.Presenter<SettingsView> {

        var settingsComponent: SettingsComponent
    }
}