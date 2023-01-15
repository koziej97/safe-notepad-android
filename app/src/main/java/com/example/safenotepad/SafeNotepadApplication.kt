package com.example.safenotepad

import android.app.Application
import com.example.safenotepad.data.database.NoteRoomDatabase

class SafeNotepadApplication: Application() {
    val database: NoteRoomDatabase by lazy { NoteRoomDatabase.getDatabase(this) }
}