package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.itsronald.twenty2020.model.Cycle
import com.itsronald.twenty2020.model.CycleModule
import com.itsronald.twenty2020.model.DaggerCycleComponent
import javax.inject.Inject


class CycleService : Service() {

    @Inject
    lateinit var cycle: Cycle

    override fun onCreate() {
        super.onCreate()
        DaggerCycleComponent.builder().cycleModule(CycleModule()).build().inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}