package com.example.safenotepad

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentFingerprintBinding
import java.util.concurrent.Executor

class FingerprintFragment : Fragment() {

    private var _binding : FragmentFingerprintBinding? = null
    private val binding get() = _binding!!

    private val mSharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFingerprintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }

        val noteTextEncrypted = encryptedSharedPreferences?.loadNote()
        if (noteTextEncrypted != "Empty note"){
            val iv = encryptedSharedPreferences?.loadIv()

            val cryptoObject = BiometricPrompt.CryptoObject(
                CryptographyUtil.getInitializedCipherForDecryption(iv)
            )
            showBiometricPrompt(
                activity = requireActivity() as AppCompatActivity,
                cryptoObject = cryptoObject
            )
        }
        else {
            mSharedViewModel.noteTextShared = noteTextEncrypted
            showBiometricPrompt(
                activity = requireActivity() as AppCompatActivity,
                cryptoObject = null
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint to ensure it's you!",
        activity: AppCompatActivity,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false
    ) {
        // Prepare BiometricPrompt Dialog
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description
        )

        // Attach with caller and callback handler
        val biometricPrompt = initBiometricPrompt(activity)

        // Authenticate with a CryptoObject if provided, otherwise default authentication
        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

    fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")


        return builder.build()
    }

    fun initBiometricPrompt(
        activity: AppCompatActivity,
    ): BiometricPrompt {
        // Attach calling Activity
        val executor = ContextCompat.getMainExecutor(activity)

        // Attach callback handlers
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // errorCode for Cancel button = 13
                // errorCode for clicking outside the biometric box = 10
                if (errorCode == 13 || errorCode == 10) {
                    Toast.makeText(context, "You MUST use biometric authentication to use this app. Closing App...", Toast.LENGTH_LONG).show()
                    // closing app
                    activity.finishAndRemoveTask()

                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { it ->
                    val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }

                    val noteTextEncrypted = encryptedSharedPreferences?.loadNote()
                    val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
                    val decryptedMessage = CryptographyUtil.decryptData(noteTextEncryptedByteArray, it)
                    mSharedViewModel.noteTextShared = decryptedMessage
                }

                findNavController().navigate(FingerprintFragmentDirections.actionFingerprintFragmentToNotesFragment())
            }

        }

        return BiometricPrompt(activity, executor, callback)
    }

}