package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.ResourceRepository
import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.Mockito.*


class CycleTest {

    @Mock
    lateinit var resources: ResourceRepository

    @Rule @JvmField
    val mockRule: MockitoRule = MockitoJUnit.rule()

    lateinit var cycle: Cycle

    @Before
    fun setup() {
        doReturn("1").`when`(resources)
                .getPreferenceString(R.string.pref_key_general_work_phase_length)
        doReturn("1").`when`(resources)
                .getPreferenceString(R.string.pref_key_general_break_phase_length)

        this.cycle = Cycle(resources)
    }

    //region tests

    @Test
    fun initialState() {
        assertThat(cycle.running, `is`(false))
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        assertThat(cycle.elapsedTime, `is`(0))
        assertThat(cycle.duration, `is`(cycle.phase.duration(resources)))
        assertThat(cycle.remainingTime, `is`(cycle.duration - cycle.elapsedTime))
    }

    @Test
    fun getPhase() {

    }

    @Test
    fun getPhaseName() {

    }

    @Test
    fun getRunning() {

    }

    @Test
    fun getElapsedTime() {

    }

    @Test
    fun getDuration() {

    }

    @Test
    fun getRemainingTime() {

    }

    @Test
    fun isFinishingPhase() {

    }

    @Test
    fun getDurationMinutes() {

    }

    @Test
    fun getElapsedTimeMinutes() {

    }

    @Test
    fun getRemainingTimeText() {

    }

    @Test
    fun start() {

    }

    @Test
    fun pause() {

    }

    @Test
    fun toggleRunning() {

    }

    @Test
    fun restartPhase() {

    }

    @Test
    fun startNextPhase() {

    }

    //endregion
}