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
        this.cycle = Cycle(resources)
    }

    //region tests

    @Test
    fun initialState() {
        doReturn("1").`when`(resources)
                .getPreferenceString(R.string.pref_key_general_work_phase_length)
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
        assertThat(cycle.running, `is`(false))
        val elapsedTimeStart = cycle.elapsedTime

        cycle.pause()
        assertThat(cycle.running, `is`(false))
        assertThat(cycle.elapsedTime, `is`(elapsedTimeStart))

        doReturn("10").`when`(resources)
                .getPreferenceString(R.string.pref_key_general_work_phase_length)

        cycle.start()
        Thread.sleep(3000)
        assertThat(cycle.running, `is`(true))
        assertThat(cycle.elapsedTime, `is`(not(elapsedTimeStart)))

        cycle.pause()
        assertThat(cycle.running, `is`(false))
        assertThat(cycle.elapsedTime, `is`(not(elapsedTimeStart)))
    }

    @Test
    fun toggleRunning() {

    }

    @Test
    fun restartPhase() {

    }

    @Test
    fun startNextPhase() {
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))

        cycle.startNextPhase()
        assertThat(cycle.phase, `is`(Cycle.Phase.BREAK))

        cycle.startNextPhase()
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
    }

    //endregion
}