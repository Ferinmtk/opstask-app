package com.simplifybiz.ops

import android.app.Application
import com.simplifybiz.ops.di.initKoin
import org.koin.android.ext.koin.androidContext

class OpsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@OpsApplication)
        }
    }
}
