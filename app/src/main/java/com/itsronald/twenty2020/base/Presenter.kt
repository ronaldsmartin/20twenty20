package com.itsronald.twenty2020.base

import android.os.Bundle

/**
 * Base interface for Presenters.
 */
interface Presenter<V : View> {

    var view: V?

    fun onCreate(bundle: Bundle?)

    fun onStart()

    fun onStop()

    fun onSaveInstanceState(outState: Bundle)

    fun onRestoreInstanceState(savedInstanceState: Bundle?)
}