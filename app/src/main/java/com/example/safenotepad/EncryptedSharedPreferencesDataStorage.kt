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

    fun savePasswordEncrypted(TEXT: String) {
        val editor = sharedPreferencesEncrypted.edit()
        editor?.putString("Password", TEXT)
        editor?.apply()
    }

    fun loadPasswordEncrypted(): String? {
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