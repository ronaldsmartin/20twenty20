package com.itsronald.twenty2020.timer

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import com.itsronald.twenty2020.base.Presenter
import com.itsronald.twenty2020.base.View
import com.itsronald.twenty2020.model.TimerControl

/**
 * Specifies the contract between the timer view and its presenter.
 */
interface TimerContract {

    companion object {
        /** Activity action: Pause the timer. */
        const val ACTION_PAUSE = "com.itsronald.twenty2020.timer.ACTION_PAUSE"
        /** Activity action: Start the timer. */
        const val ACTION_START = "com.itsronald.twenty2020.timer.ACTION_RESUME"
    }

    interface TimerView: View<UserActionsListener> {

        companion object {
            //region Tutorial state

            @IntDef(TUTORIAL_NOT_SHOWN,
                    TUTORIAL_TARGET_TIMER_START,
                    TUTORIAL_TARGET_TIMER_SKIP,
                    TUTORIAL_TARGET_TIMER_RESTART)
            @Retention(AnnotationRetention.SOURCE)
            annotation class TutorialState

            /** Tutorial is not being shown */
            const val TUTORIAL_NOT_SHOWN = 0L
            /** Tutorial is targeting the start/stop button.*/
            const val TUTORIAL_TARGET_TIMER_START = 1L
            /** Tutorial is targeting the skip button. */
            const val TUTORIAL_TARGET_TIMER_SKIP = 2L
            /** Tutorial is targeting the restart button. */
            const val TUTORIAL_TARGET_TIMER_RESTART = 3L

            //endregion

            //region Timer mode

            @IntDef(TIMER_MODE_WORK, TIMER_MODE_BREAK)
            @Retention(AnnotationRetention.SOURCE)
            annotation class TimerMode

            const val TIMER_MODE_WORK = 1L

            const val TIMER_MODE_BREAK = 2L

            //endregion
        }

        val context: Context

        val isFabAnimationAvailable: Boolean

        /** Whether or not the app bar overflow menu should be enabled. */
        var isMenuEnabled: Boolean

        /** Whether or not the screen with this view should stay awake. */
        var keepScreenOn: Boolean

        /** Whether or not this view may go full screen. This is controlled by user preference. */
        var fullScreenAllowed: Boolean

        /** The current state of the tutorial for first-time users. */
        @TutorialState
        var tutorialState: Long

        @TimerMode
        var timerMode: Long

        /**
         * Display a short tutorial to first-time users.
         *
         * @param state The state in which to show the tutorial.
         */
        fun showTutorial(@TutorialState state: Long)

        /**
         * Display work phase time text in the view.
         * @param formattedTime The text to display.
         */
        fun showWorkTimeRemaining(formattedTime: String)

        /**
         * Display break phase time text in the view.
         * @param formattedTime The text to display.
         */
        fun showBreakTimeRemaining(formattedTime: String)

        /**
         * Display work phase progress in the view.
         * @param progress The current progress value to display.
         * @param maxProgress The maximum progress value the can be displayed.
         */
        fun showWorkProgress(progress: Float, maxProgress: Float)

        /**
         * Display break phase progress in the view.
         * @param progress The current progress value to display.
         * @param maxProgress The maximum progress value the can be displayed.
         */
        fun showBreakProgress(progress: Float, maxProgress: Float)

        /**
         * Change the FloatingActionButton's drawable icon in the view.
         * @param drawableId The resource ID of the icon to display in the FAB.
         */
        fun setFABDrawable(@DrawableRes drawableId: Int, animated: Boolean = false)

        /**
         * Display a message in this view.
         * @param message The message to display to the user.
         */
        fun showMessage(message: String)
    }

    interface UserActionsListener: Presenter<TimerView>, TimerControl {

        //region Menu options

        /**
         * Open the About page.
         */
        fun openAboutApp()

        /**
         * Open the app settings.
         */
        fun openSettings()

        /**
         * Open the Help/Feedback page.
         */
        fun openHelpFeedback()

        //endregion

        /**
         * Notify the listener that the tutorial button was clicked.
         *
         * @param currentState The state of the tutorial that was clicked.
         */
        fun onTutorialNextClicked(@TimerView.Companion.TutorialState currentState: Long)

        /**
         * Callback to signify that the user finished the tutorial.
         */
        fun onTutorialFinished()

        /**
         * Notify the listener that the work timer was clicked.
         */
        fun onWorkTimerClicked()

        /**
         * Notify the listener that the break timer was clicked.
         */
        fun onBreakTimerClicked()

        /**
         * Called when an action is received (via Intent) by [view].
         *
         * For possible actions, see [TimerContract.Companion]. Other actions will not be handled
         * by the presenter.
         *
         * @param action The action received by [view].
         */
        fun onActionReceived(action: String)
    }

}