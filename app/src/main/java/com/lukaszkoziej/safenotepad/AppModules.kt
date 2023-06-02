package com.lukaszkoziej.safenotepad

import android.app.Application
import android.content.Context
import com.lukaszkoziej.safenotepad.data.database.NoteDao
import com.lukaszkoziej.safenotepad.data.database.NoteRoomDatabase
import com.lukaszkoziej.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    fun provideEncryptedSharedPreferences(androidContext: Context): EncryptedSharedPreferencesDataStorage {
        return EncryptedSharedPreferencesDataStorage(androidContext)
    }

    fun provideNoteRoomDatabase(androidApplication: Application): NoteRoomDatabase {
        return NoteRoomDatabase.getDatabase(androidApplication)
    }

    fun provideNoteDao(database: NoteRoomDatabase): NoteDao {
        return database.noteDao()
    }

    single {
        provideEncryptedSharedPreferences(androidContext())
    }
    single {
        provideNoteRoomDatabase(androidApplication())
    }
    single<NoteDao> {
        provideNoteDao(get())
    }

}

val viewModelModule = module {
    viewModel {
        SharedViewModel(noteDao = get(), sharedPreferences = get())
    }
}