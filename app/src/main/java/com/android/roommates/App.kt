package com.android.roommates

import android.app.Application
import android.content.Context

import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }

    override fun onCreate() {
        super.onCreate()

    }

}