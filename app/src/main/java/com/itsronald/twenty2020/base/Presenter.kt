package com.itsronald.twenty2020.base

import android.os.Bundle

/**
 * Base interface for Presenters.
 */
interface Presenter<View> {

    var view: View

    fun onCreate(bundle: Bundle?) = Unit

    fun onStart() = Unit

    fun onStop() = Unit

    fun onSaveInstanceState(outState: Bundle?) = Unit

    fun onRestoreInstanceState(savedInstanceState: Bundle?) = Unit
}