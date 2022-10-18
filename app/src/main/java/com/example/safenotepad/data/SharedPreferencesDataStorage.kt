package com.example.safenotepad.data

import android.content.Context
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity

class SharedPreferencesDataStorage(val context: Context) {

    fun savePassword(TEXT: String) {
        val sharedPreferences = context.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("Password", TEXT.trim())
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

    fun saveSalt(salt: ByteArray) {
        val text = Base64.encodeToString(salt, Base64.DEFAULT).trim()
        val sharedPreferences = context.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("Salt", text)
        editor?.apply()
    }

    fun loadSalt(): ByteArray {
        val sharedPreferences =
            context.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        val text = sharedPreferences?.getString("Salt", "")?.trim()
        val salt: ByteArray = Base64.decode(text, Base64.DEFAULT)
        return salt
    }

    fun saveIv(iv: ByteArray) {
        val text = Base64.encodeToString(iv, Base64.DEFAULT).trim()
        val sharedPreferences = context.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("IV", text)
        editor?.apply()
    }

    fun loadIv(): ByteArray {
        val sharedPreferences =
            context.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        val text = sharedPreferences?.getString("IV", "")
        val iv: ByteArray = Base64.decode(text, Base64.DEFAULT)
        return iv
    }

}