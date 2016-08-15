package com.itsronald.twenty2020.about

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.ResourceRepository
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import javax.inject.Inject

/**
 * Generates an "About App" page and responds to the page's user interactions.
 */
class AboutPresenter
    @Inject constructor(val resources: ResourceRepository)
    : LibsConfiguration.LibsListener {

    /**
     * Build an intent for a new About activity.
     *
     * @param context A Context used to generate the new Activity.
     *
     * @return An intent for the About activity that will be created.
     */
    fun buildIntent(context: Context): Intent = buildActivity(context = context).intent(context)

    //region AboutLibraries building

    private fun buildActivity(context: Context): LibsBuilder = LibsBuilder()
            .withAppStyle(context = context)
            .withAboutOptions(context = context)
            .withButtons()

    private fun LibsBuilder.withAppStyle(context: Context): LibsBuilder {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = nightMode == Configuration.UI_MODE_NIGHT_YES

        val activityStyle = if (isNightMode) Libs.ActivityStyle.DARK
                            else Libs.ActivityStyle.LIGHT_DARK_TOOLBAR
        val activityTitle = resources.getString(R.string.about)

        return this.withActivityStyle(activityStyle)
                .withActivityTitle(activityTitle)
    }

    private fun LibsBuilder.withAboutOptions(context: Context): LibsBuilder {
        val appName = resources.getString(context.applicationInfo.labelRes)
        return this.withAboutAppName(appName)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withLicenseShown(true)
    }

    private fun LibsBuilder.withButtons(): LibsBuilder {
        val sourceCodeButtonTitle = resources.getString(R.string.about_button_source)
        val sourceCodeButtonDescription = resources
                .getString(R.string.about_button_source_description)
        val rateAppButtonTitle = resources.getString(R.string.about_button_rate)
        val rateAppButtonDescription = resources.getString(R.string.about_button_rate_description)

        return this.withAboutSpecial1(sourceCodeButtonTitle)
                .withAboutSpecial1Description(sourceCodeButtonDescription)
                .withAboutSpecial2(rateAppButtonTitle)
                .withAboutSpecial2Description(rateAppButtonDescription)
                .withListener(this@AboutPresenter)
    }

    //endregion

    //region LibsConfiguration.LibsListener

    override fun onIconClicked(v: View?) {
        val aboutURL = resources.getString(R.string.url_about)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutURL))
        v?.context?.startActivity(intent)
    }

    override fun onIconLongClicked(v: View?): Boolean = false

    override fun onExtraClicked(v: View?, specialButton: Libs.SpecialButton?): Boolean =
            when (specialButton) {
                Libs.SpecialButton.SPECIAL1 -> onSourceCodeButtonClick(view = v)
                Libs.SpecialButton.SPECIAL2 -> onRateAppButtonClick(view = v)
                Libs.SpecialButton.SPECIAL3 -> false
                null -> throw NullPointerException()
            }

    private fun onSourceCodeButtonClick(view: View?): Boolean = false

    private fun onRateAppButtonClick(view: View?): Boolean = false

    override fun onLibraryAuthorClicked(v: View?, library: Library?): Boolean = false

    override fun onLibraryAuthorLongClicked(v: View?, library: Library?): Boolean = false

    override fun onLibraryContentClicked(v: View?, library: Library?): Boolean = false

    override fun onLibraryContentLongClicked(v: View?, library: Library?): Boolean = false

    override fun onLibraryBottomClicked(v: View?, library: Library?): Boolean = false

    override fun onLibraryBottomLongClicked(v: View?, library: Library?): Boolean = false

    //endregion
}