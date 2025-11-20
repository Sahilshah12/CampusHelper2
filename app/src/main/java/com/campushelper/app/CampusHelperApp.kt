package com.campushelper.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CampusHelperApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
