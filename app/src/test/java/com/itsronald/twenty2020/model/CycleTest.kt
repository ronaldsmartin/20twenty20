package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.ResourceRepository
import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.Matchers.*
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
        assertThat(cycle.running, `is`(false))
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        assertThat(cycle.elapsedTime, `is`(0))
        assertThat(cycle.duration, `is`(cycle.phase.duration(resources)))
        assertThat(cycle.remainingTime, `is`(cycle.duration - cycle.elapsedTime))
    }

    @Test
    fun getPhase() {
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))

        cycle.startNextPhase()
        assertThat(cycle.phase, `is`(Cycle.Phase.BREAK))
    }

    @Test
    fun getPhaseName() {
        assertThat(cycle.phaseName, `is`(cycle.phase.localizedName(resources)))
    }

    @Test
    fun getRunning() {
        assertThat(cycle.running, `is`(false))

        cycle.start()
        Thread.sleep(2000)
        assertThat(cycle.running, `is`(true))

        Thread.sleep(2000)
        assertThat(cycle.running, `is`(true))

        cycle.pause()
        Thread.sleep(2000)
        assertThat(cycle.running, `is`(false))
    }


    @Test
    fun getElapsedTime() {
        assertThat(cycle.elapsedTime, `is`(0))

        cycle.start()
        Thread.sleep(5000)
        cycle.pause()

        assertThat(cycle.elapsedTime, both(greaterThan(3)).and(lessThanOrEqualTo(5)))
    }

    @Test
    fun getDuration() {
        assertThat(cycle.duration, `is`(cycle.phase.duration(resources)))
    }

    @Test
    fun getRemainingTime() {
        assertThat(cycle.remainingTime, `is`(cycle.duration))

        cycle.start()
        Thread.sleep(5000)
        cycle.pause()

        assertThat(cycle.remainingTime, `is`(cycle.duration - cycle.elapsedTime))
    }

    @Test
    fun isFinishingPhase() {
        val newDuration = 3
        doReturn("$newDuration").`when`(resources)
                .getPreferenceString(R.string.pref_key_general_work_phase_length)

        // Skip to next work Cycle to refresh duration to stubbed value.
        cycle.startNextPhase()
        cycle.startNextPhase()
        assertThat(cycle.duration, `is`(newDuration))

        assertThat(cycle.isFinishingPhase, `is`(false))

        cycle.start()
        Thread.sleep(2500)

        assertThat(cycle.isFinishingPhase, `is`(true))
    }

    @Test
    fun getDurationMinutes() {
        assertThat(cycle.durationMinutes, `is`(cycle.duration / 60))
    }

    @Test
    fun getElapsedTimeMinutes() {
        assertThat(cycle.elapsedTimeMinutes, `is`(cycle.elapsedTime / 60))
    }

    @Test
    fun getRemainingTimeText() {

    }

    @Test
    fun start() {
        assertThat(cycle.running, `is`(false))
        val elapsedTimeStart = cycle.elapsedTime
        val remainingTimeStart = cycle.remainingTime

        cycle.start()
        Thread.sleep(2000)

        val elapsedTime2 = cycle.elapsedTime
        val remainingTime2 = cycle.remainingTime
        assertThat(cycle.running, `is`(true))
        assertThat(elapsedTime2, `is`(greaterThan(elapsedTimeStart)))
        assertThat(remainingTime2, `is`(lessThan(remainingTimeStart)))

        Thread.sleep(2000)

        assertThat(cycle.running, `is`(true))
        assertThat(cycle.elapsedTime, `is`(greaterThan(elapsedTime2)))
        assertThat(cycle.remainingTime, `is`(lessThan(remainingTime2)))
    }

    @Test
    fun startIgnoresRedundantCalls() {
        assertThat(cycle.running, `is`(false))
        val elapsedTimeStart = cycle.elapsedTime
        val remainingTimeStart = cycle.remainingTime

        cycle.start()
        Thread.sleep(2000)

        cycle.start()
        val elapsedTime2 = cycle.elapsedTime
        val remainingTime2 = cycle.remainingTime
        assertThat(cycle.running, `is`(true))
        assertThat(elapsedTime2, `is`(greaterThan(elapsedTimeStart)))
        assertThat(remainingTime2, `is`(lessThan(remainingTimeStart)))
        Thread.sleep(1000)

        cycle.start()
        assertThat(cycle.running, `is`(true))
        assertThat(cycle.elapsedTime, `is`(greaterThan(elapsedTime2)))
        assertThat(cycle.remainingTime, `is`(lessThan(remainingTime2)))
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
        assertThat(cycle.running, `is`(false))

        cycle.toggleRunning()
        assertThat(cycle.running, `is`(true))

        cycle.toggleRunning()
        assertThat(cycle.running, `is`(false))
    }

    @Test
    fun restartPhase() {
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        val duration = cycle.duration
        assertThat(cycle.elapsedTime, `is`(0))
        assertThat(cycle.remainingTime, `is`(duration))

        cycle.restartPhase()
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        assertThat(cycle.elapsedTime, `is`(0))
        assertThat(cycle.remainingTime, `is`(duration))

        cycle.start()
        Thread.sleep(5000)
        cycle.pause()
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        assertThat(cycle.elapsedTime, `is`(greaterThan(0)))
        assertThat(cycle.remainingTime, `is`(lessThan(duration)))

        cycle.restartPhase()
        assertThat(cycle.phase, `is`(Cycle.Phase.WORK))
        assertThat(cycle.elapsedTime, `is`(0))
        assertThat(cycle.remainingTime, `is`(duration))
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