package com.lukaszkoziej.safenotepad

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SafeNotepadApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SafeNotepadApplication)
            modules(
                databaseModule,
                viewModelModule
            )
        }
    }
}