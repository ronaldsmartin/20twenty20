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

    @Mock
    lateinit var resources: ResourceRepository

    @Rule @JvmField
    val mockRule: MockitoRule = MockitoJUnit.rule()

    @Test
    fun duration() {

    }

    @Test
    fun getDefaultDuration() {

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