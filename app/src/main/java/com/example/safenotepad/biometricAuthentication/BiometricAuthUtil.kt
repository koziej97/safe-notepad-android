package com.example.safenotepad.biometricAuthentication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.data.EncryptedSharedPreferencesDataStorage

class BiometricAuthUtil (val context: Context, val mSharedViewModel: SharedViewModel) {

    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint to ensure it's you!",
        activity: AppCompatActivity,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ) {
        // Prepare BiometricPrompt Dialog
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description
        )
        val biometricPrompt = initBiometricPrompt(activity)
        biometricPrompt.apply {
            if (cryptoObject == null) {
                authenticate(promptInfo)
            }
            else {
                authenticate(promptInfo, cryptoObject)
            }
        }
    }

    private fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun initBiometricPrompt(activity: AppCompatActivity): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { it ->
                    val encryptedSharedPreferences = EncryptedSharedPreferencesDataStorage(context)
                    val noteTextEncrypted = encryptedSharedPreferences.loadNote()
                    val decryptedMessage = mSharedViewModel.getDecryptedNote(noteTextEncrypted, it)
                    mSharedViewModel.noteTextShared = decryptedMessage
                }
                mSharedViewModel.isBiometricAuthSucceeded.value = true
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }
}