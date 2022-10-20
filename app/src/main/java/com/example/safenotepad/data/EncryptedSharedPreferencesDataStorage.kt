package com.example.safenotepad.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedSharedPreferencesDataStorage(val context: Context) :
    EncryptedSharedPreferencesAbstract() {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    override val sharedPreferencesEncrypted = context.let {
        EncryptedSharedPreferences.create(
            "Data",
            masterKey,
            it,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    override val editor: SharedPreferences.Editor = sharedPreferencesEncrypted.edit()

    fun savePassword(text: String){
        val key = "Password"
        super.saveString(key ,text)
    }

    fun loadPassword(): String? {
        if (!sharedPreferencesEncrypted.contains("Password")){
            return null
        }
        return super.loadString("Password")
    }

    fun saveNote(text: String) {
        val key = "Note"
        super.saveString(key ,text)
    }

    fun loadNote(): String? {
        if (!sharedPreferencesEncrypted.contains("Note")){
            return "Empty note"
        }
        return super.loadString("Note")
    }

    fun saveSalt(salt: ByteArray) {
        val key = "Salt"
        super.saveByteArray(key, salt)
    }

    fun loadSalt() : ByteArray? {
        return super.loadByteArray("Salt")
    }

    fun saveIv(iv: ByteArray) {
        val key = "IV"
        super.saveByteArray(key, iv)
    }

    fun loadIv(): ByteArray? {
        return super.loadByteArray("IV")
    }
}