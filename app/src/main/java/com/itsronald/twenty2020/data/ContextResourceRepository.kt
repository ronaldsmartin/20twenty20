package com.itsronald.twenty2020.data

import android.app.backup.BackupManager
import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat

/**
 * Concrete implementation of [ResourceRepository] that uses a [Context] to provide requested
 * resouces.
 */
class ContextResourceRepository(private val context: Context) : ResourceRepository {

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
            context.getString(resId, *formatArgs)

    override fun getColor(@ColorRes resId: Int): Int = ContextCompat.getColor(context, resId)

    override fun notifyBackupDataChanged() = BackupManager(context).dataChanged()

    override fun getPreferenceString(@StringRes keyResId: Int, prefsFilename: String?): String? {
        val key = getString(keyResId)
        val prefs = if (prefsFilename == null) PreferenceManager.getDefaultSharedPreferences(context)
            else context.getSharedPreferences(prefsFilename, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }
}