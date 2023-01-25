package com.example.safenotepad.data.sharedPreferences

import android.content.SharedPreferences
import android.util.Base64

abstract class EncryptedSharedPreferencesAbstract {

    abstract val sharedPreferencesEncrypted: SharedPreferences
    abstract val editor: SharedPreferences.Editor

    fun saveString(key: String, text: String){
        editor.putString(key, text)
        editor.apply()
    }

    fun loadString(key: String): String? {
        return sharedPreferencesEncrypted.getString(key, "")
    }

    fun saveByteArray(key: String, byteArray: ByteArray){
        val text = Base64.encodeToString(byteArray, Base64.DEFAULT).trim()
        saveString(key, text)
    }

    fun loadByteArray(key: String): ByteArray? {
        val text = loadString(key)
        return Base64.decode(text, Base64.DEFAULT)
    }

    fun removeString(key: String) {
        editor.remove(key)
        editor.apply()
    }
}