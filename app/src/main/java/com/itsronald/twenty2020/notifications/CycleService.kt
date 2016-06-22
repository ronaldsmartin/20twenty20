package com.itsronald.twenty2020.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder


class CycleService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}