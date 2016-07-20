package com.itsronald.twenty2020.timer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.settings.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.PreferencesModule
import kotlinx.android.synthetic.main.activity_timer.*
import timber.log.Timber
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TimerActivity : AppCompatActivity(), TimerContract.TimerView {

    //region Fullscreen handlers

    private val mHideHandler = Handler()

    // Normally we'd suppress "InlinedApi" here, but Kotlin doesn't support this yet.
    @SuppressLint("NewApi")
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        constraint_layout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
        controls_layout.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    //endregion

    //region Activity lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        val cycleComponent = (application as? Twenty2020Application)?.cycleComponent
        val settingsComponent = DaggerPreferencesComponent.builder()
                .preferencesModule(PreferencesModule(this))
                .build()
        val resourceComponent = DaggerResourceComponent.builder()
                .resourceModule(ResourceModule(this))
                .build()
        DaggerTimerComponent.builder()
                .cycleComponent(cycleComponent)
                .resourceComponent(resourceComponent)
                .preferencesComponent(settingsComponent)
                .timerModule(TimerModule(this))
                .build().inject(this)
        presenter.onCreate(savedInstanceState)

        mVisible = true

        setTouchListeners()
    }

    private fun setTouchListeners() {
        // Set up the user interaction to manually show or hide the system UI.
        constraint_layout.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        timer_fab.setOnTouchListener(mDelayHideTouchListener)

        btn_restart_phase.setOnClickListener { presenter.restartPhase() }
        timer_fab.setOnClickListener { fab -> presenter.toggleRunning() }
        btn_next_phase.setOnClickListener { presenter.startNextPhase() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == TimerContract.ACTION_PAUSE && presenter.running) {
            presenter.toggleRunning()
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    //endregion

    //region Menu interaction

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_settings -> {
                presenter.openSettings()
                return true
            }
            R.id.menu_help_feedback -> {
                presenter.openHelpFeedback()
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    //endregion

    //region Fullscreen interaction

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        if (!fullScreenAllowed) {
            Timber.v("Ignoring full screen hide command: full screen is not allowed.")
            return
        }

        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        controls_layout.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    // Normally we'd suppress "InlinedApi" here, but Kotlin doesn't support this yet.
    @SuppressLint("NewApi")
    private fun show() {
        // Show the system bar
        constraint_layout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        if (!fullScreenAllowed) {
            Timber.v("Ignoring full screen hide command: full screen is not allowed.")
            return
        }

        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    //endregion

    //region TimerContract.TimerView

    override val context: Context = this

    override var keepScreenOn: Boolean
        get() = constraint_layout.keepScreenOn
        set(value) { constraint_layout.keepScreenOn = value }

    override var fullScreenAllowed = false

    @Inject
    override lateinit var presenter: TimerContract.UserActionsListener

    override fun showFirstTimeTutorial() {
        ShowcaseView.Builder(this)
                .setContentTitle(R.string.tutorial_content_title_start)
                .setContentText(R.string.tutorial_content_message_start)
                .setTarget(ViewTarget(timer_fab))
                .withMaterialShowcase()
                .build()
    }

    override fun showTimeRemaining(formattedTime: String) {
        center_text.text = formattedTime
    }

    override fun showMajorProgress(progress: Int, maxProgress: Int) {
        val progressPercent = (progress.toDouble() / maxProgress.toDouble()) * 100
        wave_view.setProgress(progressPercent.toInt())
    }

    override fun showMinorProgress(progress: Int, maxProgress: Int) {
        throw UnsupportedOperationException()
    }

    override fun setFABDrawable(@DrawableRes drawableId: Int) {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        timer_fab.setImageDrawable(drawable)
    }

    override fun showMessage(message: String) {
        Timber.v("Showing message in view: \"$message\"")
        ViewCompat.setFitsSystemWindows(coordinator_layout, true)
        Snackbar.make(coordinator_layout, message, Snackbar.LENGTH_SHORT)
                .show()
    }

    //endregion

    //region CoordinatorLayout.Behavior

    /**
     * A custom CoordinatorLayout Behavior that pushes the layout child up when a Snackbar is
     * shown in the layout.
     */
    class SnackbarPushesUpBehavior(context: Context, attributeSet: AttributeSet)
            : CoordinatorLayout.Behavior<View>(context, attributeSet) {

        override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?,
                                     dependency: View?): Boolean =
                dependency is Snackbar.SnackbarLayout

        override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
            // Based off of http://stackoverflow.com/a/32805667/4499783
            if (dependency == null) return false

            child?.translationY = Math.min(0f, dependency.translationY - dependency.height)
            return true
        }
    }

    //endregion

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
