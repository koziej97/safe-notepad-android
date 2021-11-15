package com.example.safenotepad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class SharedPreferencesDataStorage(val context: Context) {

    fun savePassword(TEXT: String) {
        val sharedPreferences = context.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("Password", TEXT)
        editor?.apply()
    }

    fun loadPassword(): String? {
        val sharedPreferences =
            context.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences?.contains("Password") == false){
            return "0000"
        }
        return sharedPreferences?.getString("Password", "")
    }

    fun saveNote(TEXT: String) {
        val sharedPreferences = context.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("Note", TEXT)
        editor?.apply()
    }

    fun loadNote(): String? {
        val sharedPreferences =
            context.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences?.contains("Note") == false){
            return "Empty note"
        }
        return sharedPreferences?.getString("Note", "")
    }

}