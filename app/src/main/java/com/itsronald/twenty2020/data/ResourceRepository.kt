package com.itsronald.twenty2020.data

import android.support.annotation.ColorRes
import android.support.annotation.StringRes

/**
 * An abstraction over the access of resources through the app.
 */
interface ResourceRepository {

    /**
     * Returns a localized formatted string from the application's package's default string table,
     * substituting the format arguments as defined in Formatter and format(String, Object...).
     *
     * @param resId Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution. By default, no
     * arguments are provided.
     *
     * @return The string data associated with the resource, formatted and stripped of styled text information.
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any = emptyArray()): String

    /**
     * Returns a color associated with a particular resource ID.
     * Starting in Build.VERSION_CODES.M, the returned color will be styled for the specified Context's theme.
     *
     * @param resId The desired resource identifier, as generated by the aapt tool. This integer
     * encodes the package, type, and resource entry. The value 0 is an invalid identifier.
     *
     * @return A single color value in the form 0xAARRGGBB.
     *
     */
    fun getColor(@ColorRes resId: Int): Int

    /**
     * Notify the resource repository that resources are available to be backed up.
     */
    fun notifyBackupDataChanged()
}