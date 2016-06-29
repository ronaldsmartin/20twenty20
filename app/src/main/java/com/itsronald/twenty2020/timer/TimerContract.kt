package com.itsronald.twenty2020.timer

import android.support.annotation.DrawableRes
import com.itsronald.twenty2020.base.Presenter
import com.itsronald.twenty2020.base.View
import com.itsronald.twenty2020.model.TimerControl

/**
 * Specifies the contract between the timer view and its presenter.
 */
interface TimerContract {

    companion object {
        /** Activity action: Pause the timer. */
        val ACTION_PAUSE = "com.itsronald.twenty2020.timer.ACTION_PAUSE"
    }

    interface TimerView: View<UserActionsListener> {

        /**
         * Whether or not the screen with this view should stay awake.
         */
        var keepScreenOn: Boolean

        /**
         * Display time text in the view.
         * @param formattedTime The text to display.
         */
        fun showTimeRemaining(formattedTime: String)

        /**
         * Display major progress in the view.
         * @param progress The current progress value to display.
         * @param maxProgress The maximum progress value the can be displayed.
         */
        fun showMajorProgress(progress: Int, maxProgress: Int)

        /**
         * Display minor progress in the view.
         * @param progress The current progress value to display.
         * @param maxProgress The maximum progress value the can be displayed.
         */
        fun showMinorProgress(progress: Int, maxProgress: Int)

        /**
         * Change the FloatingActionButton's drawable icon in the view.
         * @param drawableId The resource ID of the icon to display in the FAB.
         */
        fun setFABDrawable(@DrawableRes drawableId: Int)
    }

    interface UserActionsListener: Presenter<TimerView>, TimerControl {

        /// Menu options

        /**
         * Open the app settings.
         */
        fun openSettings()

        /**
         * Open the Help/Feedback page.
         */
        fun openHelpFeedback()
    }

}