package com.itsronald.twenty2020.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.os.Build
import android.preference.PreferenceManager

/**
 * A custom agent to handle Android backup of the app's SharedPreferences.
 *
 * See https://developer.android.com/training/backup/backupapi.html
 */
class Twenty2020BackupAgent : BackupAgentHelper() {

    /** Backup key for SharedPreferences. */
    private val DEFAULT_SHARED_PREFS_BACKUP_KEY: String
        get() = "${applicationContext.packageName}.backup.preferences"

    /** Name of the default SharedPreferences file. */
    private val defaultSharedPreferencesName: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PreferenceManager.getDefaultSharedPreferencesName(applicationContext)
        } else {
            // While the `getDefaultSharedPreferencesName` API is not exposed pre API-24, this
            // is the return value used internally.
            "${applicationContext.packageName}_preferences"
        }

    //region lifecycle

    override fun onCreate() {
        super.onCreate()

        // Backup default SharedPreferences.
        val sharedPrefsBackupHelper =
                SharedPreferencesBackupHelper(this, defaultSharedPreferencesName)
        addHelper(DEFAULT_SHARED_PREFS_BACKUP_KEY, sharedPrefsBackupHelper)
    }

    //endregion
}