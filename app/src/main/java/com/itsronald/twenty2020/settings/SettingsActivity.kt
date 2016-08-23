package com.itsronald.twenty2020.settings


import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.support.annotation.StringRes
import android.support.v4.app.NavUtils
import android.text.TextUtils
import android.view.MenuItem
import android.view.ViewGroup
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.settings.injection.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.injection.DaggerSettingsComponent
import com.itsronald.twenty2020.settings.injection.PreferencesModule
import com.itsronald.twenty2020.settings.injection.SettingsModule
import timber.log.Timber
import javax.inject.Inject

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
   * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
   * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity(), SettingsContract.SettingsView {

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(com.itsronald.twenty2020.R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, ""))
        }

        /**
         * Tag used to identify this activity's [SettingsFragment].
         */
        private val TAG_SETTINGS_FRAGMENT = SettingsFragment::class.java.canonicalName
    }

    //region Activity lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

        val fragmentTransaction = fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment(), TAG_SETTINGS_FRAGMENT)
        // Force the transaction to occur synchronously. Otherwise, the fragment will still be null
        // by the time the presenter's onCreate() is called, which in turn may ask the fragment to
        // remove a preference. The alternate would be to post onCreate() after a delay or to start
        // the removal in onStart(), but those de-synchronize the presenter from the activity lifecycle.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentTransaction.commitNow()
        } else {
            fragmentTransaction.commit()
            fragmentManager.executePendingTransactions()
        }

        val preferencesComponent = DaggerPreferencesComponent.builder()
                .preferencesModule(PreferencesModule(this))
                .build()
        val resourceComponent = DaggerResourceComponent.builder()
                .resourceModule(ResourceModule(this))
                .build()
        DaggerSettingsComponent.builder()
                .preferencesComponent(preferencesComponent)
                .resourceComponent(resourceComponent)
                .settingsModule(SettingsModule(this))
                .build()
                .inject(this)
        presenter.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> {
            NavUtils.navigateUpFromSameTask(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Set up the ActionBar, if the API is available.
     */
    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onBackPressed() {
        // 2016-08-21, using Support Libs v24.2.0
        // When the back button is pressed to finish this activity, night mode changes are not
        // applied in Marshmallow. however, using the up button does this correctly.
        Timber.v("Back button pressed.")
        NavUtils.navigateUpFromSameTask(this)
    }

    //endregion

    //region SettingsContract.SettingsView

    override val contentView: ViewGroup
        get() = findViewById(android.R.id.content) as ViewGroup

    @Inject
    override lateinit var presenter: SettingsContract.Presenter

    /** The SettingsFragment managing the PreferenceScreen. */
    private val settingsFragment: SettingsFragment
        get() = fragmentManager.findFragmentByTag(TAG_SETTINGS_FRAGMENT) as SettingsFragment

    override fun refreshNightMode(nightMode: Int) {
        val applyDayNight = delegate.applyDayNight()
        Timber.i("Applying DayNight mode: $applyDayNight")
        if (applyDayNight) recreate()
    }

    override fun setPreferenceChecked(@StringRes prefKeyID: Int, checked: Boolean): Boolean {
        val preference = settingsFragment.findPreference(getString(prefKeyID)) as? TwoStatePreference
        if (preference == null) {
            return false
        } else {
            preference.isChecked = checked
            return true
        }
    }

    override fun setPreferenceEnabled(@StringRes prefKeyID: Int, enabled: Boolean): Boolean =
            settingsFragment.findPreference(getString(prefKeyID))?.let {
                it.isEnabled = enabled
                true
            } ?: false

    override fun removePreference(@StringRes prefKeyID: Int, @StringRes inCategory: Int?): Boolean {
        val preferenceGroup: PreferenceGroup =
                inCategory?.let { settingsFragment.findPreference(getString(it)) as? PreferenceGroup }
                ?: settingsFragment.preferenceScreen

        val prefKey = getString(prefKeyID)
        return settingsFragment.findPreference(prefKey)?.let {
            Timber.i("Removing preference: $it")
            preferenceGroup.removePreference(it)
        } ?: false
    }

    //endregion

    /**
     * Creates and manages the layout for Settings management.
     * Settings are defined in [R.xml.preferences].
     */
    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            bindPreferenceSummariesToValues(
                    R.string.pref_key_general_work_phase_length,
                    R.string.pref_key_general_break_phase_length,
                    R.string.pref_key_display_night_mode,
                    R.string.pref_key_notifications_ringtone
            )
        }

        /**
         * Binds preference summaries to their values. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see bindPreferenceSummaryToValue
         */
        private fun bindPreferenceSummariesToValues(@StringRes vararg prefIDs: Int) {
            for (@StringRes prefID in prefIDs) {
                val preference = findPreference(getString(prefID))
                bindPreferenceSummaryToValue(preference)
            }
        }
    }
}
