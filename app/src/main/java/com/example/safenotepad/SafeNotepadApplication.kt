package com.example.safenotepad

import android.app.Application
import android.content.Context
import com.example.safenotepad.data.database.NoteDao
import com.example.safenotepad.data.database.NoteRoomDatabase
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

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