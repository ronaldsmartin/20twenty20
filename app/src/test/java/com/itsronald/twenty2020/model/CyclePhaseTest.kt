package com.itsronald.twenty2020.model

import org.junit.Test

import org.junit.Assert.*
import org.hamcrest.CoreMatchers.*


class CyclePhaseTest {
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

    }

}