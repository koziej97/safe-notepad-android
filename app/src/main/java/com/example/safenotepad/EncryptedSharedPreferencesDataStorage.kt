package com.example.safenotepad

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class EncryptedSharedPreferencesDataStorage(val context: Context)  {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferencesEncrypted = context.let {
        EncryptedSharedPreferences.create(
            "Data",
            masterKey,
            it,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun savePassword(TEXT: String) {
        val editor = sharedPreferencesEncrypted.edit()
        editor?.putString("Password", TEXT)
        editor?.apply()
    }

    fun loadPassword(): String? {
        if (!sharedPreferencesEncrypted.contains("Password")){
            return null
        }
        return sharedPreferencesEncrypted.getString("Password", "")
    }

    fun saveNote(TEXT: String) {
        val editor = sharedPreferencesEncrypted.edit()
        editor?.putString("Note", TEXT)
        editor?.apply()
    }

    fun loadNote(): String? {
        if (!sharedPreferencesEncrypted.contains("Note")){
            return "Empty note"
        }
        return sharedPreferencesEncrypted.getString("Note", "")
    }

    fun saveSalt(salt: ByteArray) {
        val text = Base64.encodeToString(salt, Base64.DEFAULT).trim()
        val editor = sharedPreferencesEncrypted.edit()
        editor?.putString("Salt", text)
        editor?.apply()
    }

    fun loadSalt(): ByteArray {
        val text = sharedPreferencesEncrypted.getString("Salt", "")?.trim()
        val salt: ByteArray = Base64.decode(text, Base64.DEFAULT)
        return salt
    }

    fun saveIv(iv: ByteArray) {
        val text = Base64.encodeToString(iv, Base64.DEFAULT).trim()
        val editor = sharedPreferencesEncrypted.edit()
        editor?.putString("IV", text)
        editor?.apply()
    }

    fun loadIv(): ByteArray {
        val text = sharedPreferencesEncrypted.getString("IV", "")
        val iv: ByteArray = Base64.decode(text, Base64.DEFAULT)
        return iv
    }
}