package com.addev.listaspam

import android.app.Application
import com.google.android.material.color.DynamicColors

class SpamCallBlockerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
