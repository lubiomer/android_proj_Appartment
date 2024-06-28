package com.management.roomates

import android.app.Application
import android.content.res.Configuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = resources.configuration
        configuration.setLayoutDirection(java.util.Locale.ENGLISH)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    companion object{
        var loggedUserApartmentId:String = ""
        var loggedUserId:String = ""
    }
}
