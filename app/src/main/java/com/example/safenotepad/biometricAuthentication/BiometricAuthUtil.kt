package com.example.safenotepad.biometricAuthentication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.safenotepad.R
import com.example.safenotepad.SharedViewModel

class BiometricAuthUtil (val context: Context, val mSharedViewModel: SharedViewModel) {

    fun showBiometricPrompt(
        title: String = context.resources.getString(R.string.biometric_authentication),
        subtitle: String = context.resources.getString(R.string.enter_biometric_credentials),
        description: String = context.resources.getString(R.string.input_your_fingerprint),
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
            .setNegativeButtonText(context.resources.getString(R.string.cancel))
            .build()
    }

    private fun initBiometricPrompt(activity: AppCompatActivity): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                mSharedViewModel.isBiometricAuthSucceeded.value = true
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }
}