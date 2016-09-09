package com.itsronald.twenty2020.timer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.Twenty2020Application
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.settings.injection.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.injection.PreferencesModule
import com.itsronald.twenty2020.timer.TimerContract.TimerView.Companion.TutorialState
import com.itsronald.twenty2020.timer.TimerContract.TimerView.Companion.TimerMode
import kotlinx.android.synthetic.main.activity_timer.*
import me.tankery.lib.circularseekbar.CircularSeekBar
import timber.log.Timber
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TimerActivity : AppCompatActivity(), TimerContract.TimerView {

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300L

        /** Bundle extra key for preserving [timerMode] across config changes. */
        private const val EXTRA_TIMER_MODE = "com.itsronald.twenty2020.timer.view.timer_mode"

        /**
         * @return An explicit intent to start this activity.
         */
        fun intent(context: Context): Intent = Intent(context, TimerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    //region Fullscreen handlers

    private val mHideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        secondaryControls.forEach { it.visibility = View.VISIBLE }
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

    private val secondaryControls: Array<View>
        get() = arrayOf(btn_restart_phase)

    //endregion

    //region Activity lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        injectDependencies()

        presenter.onCreate(savedInstanceState)

        mVisible = true

        setTouchListeners()
    }

    private fun injectDependencies() {
        val appComponent = (application as? Twenty2020Application)?.appComponent
        val settingsComponent = DaggerPreferencesComponent.builder()
                .preferencesModule(PreferencesModule(this))
                .build()
        val resourceComponent = DaggerResourceComponent.builder()
                .resourceModule(ResourceModule(this))
                .build()
        DaggerTimerComponent.builder()
                .applicationComponent(appComponent)
                .resourceComponent(resourceComponent)
                .preferencesComponent(settingsComponent)
                .timerModule(TimerModule(this))
                .build().inject(this)
    }

    private fun setTouchListeners() {
        // Set up the user interaction to manually show or hide the system UI.
        content_layout.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        timer_fab.setOnTouchListener(mDelayHideTouchListener)

        btn_restart_phase.setOnClickListener { presenter.restartPhase() }
        timer_fab.setOnClickListener { presenter.toggleRunning() }

        btn_switch_phase_work.setOnClickListener { presenter.onWorkTimerClicked() }
        btn_switch_phase_break.setOnClickListener { presenter.onBreakTimerClicked() }

        // TODO: Reenable these when timer seeking feature is implemented.
        work_seek_bar.isEnabled = false
        break_seek_bar.isEnabled = false
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        savedInstanceState?.getLong(EXTRA_TIMER_MODE)?.let { timerMode = it }
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
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

    override fun onRestart() {
        val applyDayNight = delegate.applyDayNight()
        Timber.v("Applying DayNight mode: $applyDayNight")
        super.onRestart()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putLong(EXTRA_TIMER_MODE, timerMode)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    //endregion

    //region Menu interaction

    override var isMenuEnabled: Boolean = false
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return isMenuEnabled && super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
        R.id.menu_about -> {
            presenter.openAboutApp()
            true
        }
        R.id.menu_settings -> {
            presenter.openSettings()
            true
        }
        R.id.menu_help_feedback -> {
            presenter.openHelpFeedback()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
        supportActionBar?.hide()
        secondaryControls.forEach { it.visibility = View.GONE }
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY)
    }

    // Normally we'd suppress "InlinedApi" here, but Kotlin doesn't support this yet.
    @SuppressLint("NewApi")
    private fun show() {
        // Show the system bar
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY)
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
        get() = content_layout.keepScreenOn
        set(value) { content_layout.keepScreenOn = value }

    override var fullScreenAllowed = false

    @Inject
    override lateinit var presenter: TimerContract.UserActionsListener

    //region Tutorial display

    @TutorialState
    override var tutorialState = TimerContract.TimerView.TUTORIAL_NOT_SHOWN

    private var showcaseView: ShowcaseView? = null

    override fun showTutorial(@TutorialState state: Long) = when (state) {
        TimerContract.TimerView.TUTORIAL_TARGET_TIMER_START -> {
            tutorialState = state
            showTutorialStart()
        }
        TimerContract.TimerView.TUTORIAL_TARGET_TIMER_SKIP -> {
            tutorialState = state

            showcaseView?.setContentTitle(getString(R.string.tutorial_content_title_skip_phase))
            showcaseView?.setContentText(getString(R.string.tutorial_content_message_skip_phase))
            showcaseView?.setShowcase(ViewTarget(break_container), true)

            Timber.v("Tutorial shown in state TUTORIAL_TARGET_TIMER_SKIP.")
        }
        TimerContract.TimerView.TUTORIAL_TARGET_TIMER_RESTART -> {
            tutorialState = state

            showcaseView?.setContentTitle(getString(R.string.tutorial_content_title_restart_phase))
            showcaseView?.setContentText(getString(R.string.tutorial_content_message_restart_phase))
            showcaseView?.setButtonText(getString(R.string.tutorial_button_title_done))
            showcaseView?.setShowcase(ViewTarget(btn_restart_phase), true)

            Timber.v("Tutorial shown in state TUTORIAL_TARGET_TIMER_RESTART.")
        }
        TimerContract.TimerView.TUTORIAL_NOT_SHOWN -> {
            tutorialState = state
            showcaseView?.setOnClickListener(null)
            showcaseView?.hide()
            showcaseView = null
            presenter.onTutorialFinished()
            Timber.v("Tutorial hidden.")
        }
        else -> throw IllegalArgumentException("$state is not a valid @TutorialState value.")
    }

    private fun showTutorialStart() {
        if (showcaseView != null) {
            Timber.v("Tutorial is already present. Not creating a new ShowcaseView.")
            return
        }

        showcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .blockAllTouches()
                .setContentTitle(R.string.tutorial_content_title_start)
                .setContentText(R.string.tutorial_content_message_start)
                .setTarget(ViewTarget(timer_fab))
                .setStyle(R.style.TutorialTheme)
                .setOnClickListener { presenter.onTutorialNextClicked(tutorialState) }
                .build()

        showcaseView?.setButtonPosition(showcaseViewButtonPositionParams())

        Timber.v("Tutorial shown in state TUTORIAL_TARGET_TIMER_START.")
    }

    private fun showcaseViewButtonPositionParams(): RelativeLayout.LayoutParams {
        val nextButtonLayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        nextButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        nextButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        nextButtonLayoutParams.bottomMargin = resources
                .getDimensionPixelOffset(R.dimen.activity_vertical_margin)
        return nextButtonLayoutParams
    }


    //endregion

    @TimerMode
    override var timerMode: Long = TimerContract.TimerView.TIMER_MODE_WORK
        set(value) {
            Timber.v("timerMode changed to $value.")
            field = value
            when (value) {
                TimerContract.TimerView.TIMER_MODE_WORK -> {
                    Timber.v("Switching TimerMode to TIMER_MODE_WORK.")
                    unfocusTimer(seekBar = break_seek_bar)
                    focusTimer(seekBar = work_seek_bar)
                }
                TimerContract.TimerView.TIMER_MODE_BREAK -> {
                    Timber.v("Switching TimerMode to TIMER_MODE_BREAK.")
                    unfocusTimer(seekBar = work_seek_bar)
                    focusTimer(seekBar = break_seek_bar)
                }
            }
        }

    private fun focusTimer(seekBar: CircularSeekBar) {
        bringSeekbarToFront(seekBar)
//        seekBar.isEnabled = false    TODO: Reenable when timer seeking is implemented.
        seekBar.pointerAlpha = 1
        val elevation = resources.getDimensionPixelSize(R.dimen.timer_ring_elevation_focused)
        ViewCompat.setElevation(seekBar, elevation.toFloat())

        (seekBar.parent as? View)?.alpha = 1f
    }

    private fun bringSeekbarToFront(seekBar: View) {
        val timerContainer = seekBar.parent
        if (timerContainer is View) {
            timerContainer.bringToFront()
            val containerParent = seekBar.parent.parent
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT
                    && containerParent is View) {
                containerParent.requestLayout()
                containerParent.invalidate()
            }
        }
    }

    private fun unfocusTimer(seekBar: CircularSeekBar) {
        seekBar.isEnabled = false
        seekBar.pointerAlpha = 0

        val elevation = resources.getDimensionPixelSize(R.dimen.timer_ring_elevation_unfocused)
        ViewCompat.setElevation(seekBar, elevation.toFloat())

        (seekBar.parent as? View)?.alpha = 0.65f

        animate(seekBar, toProgress = 0f)
    }

    override fun showWorkTimeRemaining(formattedTime: String) {
        work_text.setTime(formattedTime)
    }

    override fun showBreakTimeRemaining(formattedTime: String) {
        break_text.setTime(formattedTime)
    }

    override fun showWorkProgress(progress: Float, maxProgress: Float) {
        if (work_seek_bar.max != maxProgress) {
            work_seek_bar.max = maxProgress
        }
        animate(seekBar = work_seek_bar, toProgress = -progress)
    }

    override fun showBreakProgress(progress: Float, maxProgress: Float) {
        if (break_seek_bar.max != maxProgress) {
            break_seek_bar.max = maxProgress
        }
        animate(seekBar = break_seek_bar, toProgress = progress)
    }

    private fun animate(seekBar: CircularSeekBar, toProgress: Float): ObjectAnimator {
        val objectAnimator = ObjectAnimator
                .ofFloat(seekBar, "progress", seekBar.progress, toProgress)
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.duration = 2000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            objectAnimator.setAutoCancel(true)
        }

        objectAnimator.start()
        return objectAnimator
    }

    override fun setFABDrawable(@DrawableRes drawableId: Int) {
        val drawable = AppCompatResources.getDrawable(this, drawableId)
        timer_fab.setImageDrawable(drawable)
    }

    override fun showMessage(message: String) {
        Timber.v("Showing message in view: \"$message\"")
        Snackbar.make(coordinator_layout, message, Snackbar.LENGTH_SHORT)
                .show()
    }

    //endregion
}
