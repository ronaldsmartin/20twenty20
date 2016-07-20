package com.itsronald.twenty2020.model

import com.itsronald.twenty2020.R
import com.itsronald.twenty2020.data.ResourceRepository
import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.Mockito.*


class CyclePhaseTest {

    //region Mock objects

    @Mock
    lateinit var resources: ResourceRepository

    @Rule @JvmField
    val mockRule: MockitoRule = MockitoJUnit.rule()

    //endregion

    @Test
    fun durationKey() {
        Cycle.Phase.WORK.duration(resources = resources)
        verify(resources).getPreferenceString(R.string.pref_key_general_work_phase_length)

        Cycle.Phase.BREAK.duration(resources = resources)
        verify(resources).getPreferenceString(R.string.pref_key_general_break_phase_length)
    }

    @Test
    fun durationFallsBackToDefault() {
        `when`(resources.getPreferenceString(R.string.pref_key_general_work_phase_length))
                .thenReturn(null)
        `when`(resources.getPreferenceString(R.string.pref_key_general_break_phase_length))
                .thenReturn(null)

        val workPhase = Cycle.Phase.WORK
        assertThat(workPhase.duration(resources = resources), `is`(workPhase.defaultDuration))

        val breakPhase = Cycle.Phase.BREAK
        assertThat(breakPhase.duration(resources = resources), `is`(breakPhase.defaultDuration))
    }

    @Test
    fun durationFollowsPrefs() {
        `when`(resources.getPreferenceString(R.string.pref_key_general_work_phase_length))
                .thenReturn("100")
                .thenReturn("3600")
        `when`(resources.getPreferenceString(R.string.pref_key_general_break_phase_length))
                .thenReturn("200")
                .thenReturn("3700")

        val workPhase = Cycle.Phase.WORK
        val breakPhase = Cycle.Phase.BREAK

        assertThat(workPhase.duration(resources = resources), `is`(100))
        assertThat(breakPhase.duration(resources = resources), `is`(200))

        // Now mock a preference change
        assertThat(workPhase.duration(resources = resources), `is`(3600))
        assertThat(breakPhase.duration(resources = resources), `is`(3700))
    }

    @Test
    fun getDefaultDuration() {
        assertThat(Cycle.Phase.WORK.defaultDuration, `is`(20 * 60)) // 20 minutes
        assertThat(Cycle.Phase.BREAK.defaultDuration, `is`(20))     // 20 seconds
    }

    @Test
    fun getNextPhase() {
        assertThat(Cycle.Phase.WORK.nextPhase, `is`(Cycle.Phase.BREAK))
        assertThat(Cycle.Phase.BREAK.nextPhase, `is`(Cycle.Phase.WORK))
    }

    @Test
    fun localizedName() {
        Cycle.Phase.WORK.localizedName(resources)
        verify(resources).getString(R.string.phase_name_work)

        Cycle.Phase.BREAK.localizedName(resources)
        verify(resources).getString(R.string.phase_name_break)
    }

}