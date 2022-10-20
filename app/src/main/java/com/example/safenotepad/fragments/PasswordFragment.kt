package com.example.safenotepad.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.*
import com.example.safenotepad.cryptography.CryptographyUtil
import com.example.safenotepad.data.EncryptedSharedPreferencesDataStorage
import com.example.safenotepad.databinding.FragmentPasswordBinding

class PasswordFragment : Fragment() {

    var typedPassword =  MutableLiveData<String>()
    private var _binding: FragmentPasswordBinding? = null
    private val mSharedViewModel: SharedViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            passwordFragment = this@PasswordFragment
            sharedViewModel = mSharedViewModel
        }

        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }

        val correctPassword = encryptedSharedPreferences?.loadPassword()
        if (correctPassword != null) {
            mSharedViewModel.correctPassword = correctPassword
        }
        else {
            createAlertForFirstPassword()
        }


        binding.buttonPassword.setOnClickListener {
            val typedPasswordString = typedPassword.value

            if (typedPasswordString != null){

                // Doing first hash
                val salt = encryptedSharedPreferences?.loadSalt()
                mSharedViewModel.salt = salt!!
                val key = salt.let { it1 -> CryptographyUtil.calculateKey(typedPasswordString, it1) }
                val hashedPassword = key.let { it1 -> CryptographyUtil.hashFromKey(it1) }.trim()

                // Doing second hash
                val key2 = salt.let { it1 -> CryptographyUtil.calculateKey(hashedPassword, it1) }
                val hashedPassword2 = key2.let { it1 -> CryptographyUtil.hashFromKey(it1) }.trim()

                if (hashedPassword2 == mSharedViewModel.correctPassword){
                    //mSharedViewModel.hashedPasswordForKey = hashedPassword

                    //using keystore
                    val noteTextEncrypted = encryptedSharedPreferences.loadNote()
                    if (noteTextEncrypted != "Empty note"){
                        val iv = encryptedSharedPreferences.loadIv()
                        val cipher = CryptographyUtil.getInitializedCipherForDecryption(iv)
                        val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
                        val decryptedMessage =
                            CryptographyUtil.decryptData(noteTextEncryptedByteArray, cipher)
                        mSharedViewModel.noteTextShared = decryptedMessage
                    }
                    else {
                        mSharedViewModel.noteTextShared = noteTextEncrypted
                    }

                    findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
                else {
                    Toast.makeText(context, "Wrong Password!", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.buttonBiometric.setOnClickListener {
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun createAlertForFirstPassword(){
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }

        val firstPasswordEditText = EditText(activity)
        firstPasswordEditText.hint = "Type your password"
        firstPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance();

        AlertDialog.Builder(requireActivity())
            .setTitle("Set up your password")
            .setView(firstPasswordEditText)
            .setPositiveButton("OK") { _, _ ->
                var correctPassword = firstPasswordEditText.text.toString()

                if (correctPassword != ""){
                    val salt = CryptographyUtil.generateSalt()
                    encryptedSharedPreferences?.saveSalt(salt)
                    mSharedViewModel.salt = salt
                    // Doing first hash
                    val key = salt.let { it1 -> CryptographyUtil.calculateKey(correctPassword, it1) }
                    val hashedPassword = key.let { it1 -> CryptographyUtil.hashFromKey(it1) }.trim()
                    // Doing second hash
                    val key2 = salt.let { it1 -> CryptographyUtil.calculateKey(hashedPassword, it1) }
                    val hashedPassword2 = key2.let { it1 -> CryptographyUtil.hashFromKey(it1) }.trim()
                    mSharedViewModel.correctPassword = hashedPassword2
                    correctPassword = hashedPassword2
                    encryptedSharedPreferences?.savePassword(hashedPassword2)

                    Toast.makeText(context, "Now type your set password to get access", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, "You MUST set a password!", Toast.LENGTH_LONG).show()
                    createAlertForFirstPassword()
                }
            }
            .setCancelable(false)
            .create()
            .show()
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
                    val decryptedMessage =
                        CryptographyUtil.decryptData(noteTextEncryptedByteArray, it)
                    mSharedViewModel.noteTextShared = decryptedMessage
                }

                findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
            }

        }

        return BiometricPrompt(activity, executor, callback)
    }
}