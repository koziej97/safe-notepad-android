package com.lukaszkoziej.safenotepad.data.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
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

    fun saveSalt(salt: ByteArray) {
        val key = "Salt"
        super.saveByteArray(key, salt)
    }

    fun loadSalt() : ByteArray? {
        return super.loadByteArray("Salt")
    }

    fun saveIv(key: String, iv: ByteArray) {
        super.saveByteArray(key, iv)
    }

    fun loadIv(key: String): ByteArray? {
        return super.loadByteArray(key)
    }

    fun removeIv(key: String) {
        return super.removeString(key)
    }
}