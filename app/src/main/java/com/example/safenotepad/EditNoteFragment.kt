package com.example.safenotepad

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentEditNoteBinding

import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import javax.crypto.Cipher
import javax.crypto.SecretKey


class EditNoteFragment : Fragment() {
    private var _binding: FragmentEditNoteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mSharedViewModel: SharedViewModel by activityViewModels()

    var newNote = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            editNoteFragment = this@EditNoteFragment
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        // CODE WORKING WITH PASSWORD
        /*
        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()

        // load and decrypt Note
        val noteTextEncrypted = encryptedSharedPreferences?.loadNote()
        if (noteTextEncrypted != "Empty note"){
            val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
            val key = SecurityData.calculateKey(mSharedViewModel.hashedPasswordForKey, mSharedViewModel.salt)
            val iv = encryptedSharedPreferences?.loadIv()
            val noteTextDecrypted = SecurityData.decrypt(key, noteTextEncryptedByteArray, iv!!)
            newNote.value = noteTextDecrypted
        }
        else {
            newNote.value = noteTextEncrypted
        }


        binding.saveEditButton.setOnClickListener {
            val newNoteString = newNote.value
            val key = SecurityData.calculateKey(mSharedViewModel.hashedPasswordForKey, mSharedViewModel.salt)

            if (newNoteString != null) {
                val ivNew = SecurityData.generateIv()
                encryptedSharedPreferences.saveIv(ivNew)

                val newNoteEncrypted = SecurityData.encrypt(key, newNoteString, ivNew)
                val newNoteEncryptedText = Base64.encodeToString(newNoteEncrypted, Base64.DEFAULT).trim()
                encryptedSharedPreferences.saveNote(newNoteEncryptedText)
            }

            findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
        }
         */
        ///////////////////////////////////////////////////////////////////////////////////////////

        newNote.value = mSharedViewModel.noteTextShared

        binding.saveEditButton.setOnClickListener {
            val newNoteString = newNote.value

            if (newNoteString != null) {
                val cryptoObject = BiometricPrompt.CryptoObject(
                    CryptographyUtil.getInitializedCipherForEncryption()
                )

                showBiometricPrompt(
                    activity = requireActivity() as AppCompatActivity,
                    cryptoObject = cryptoObject
                )
            }

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
            description,
            allowDeviceCredential
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
        allowDeviceCredential: Boolean
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            setNegativeButtonText("Cancel")
        }

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
                result.cryptoObject?.cipher?.let {
                    val newNoteString = newNote.value
                    if (newNoteString != null) {
                        encryptAndSave(newNoteString, it)
                        mSharedViewModel.noteTextShared = newNoteString
                    }
                    findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
                }
            }
        }

        return BiometricPrompt(activity, executor, callback)
    }

    private fun encryptAndSave(plainTextMessage: String, cipher: Cipher) {
        val encryptedMessage = CryptographyUtil.encryptData(plainTextMessage, cipher)
        // Save Encrypted Message
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        encryptedSharedPreferences?.saveNote(Base64.encodeToString(encryptedMessage, Base64.DEFAULT).trim())
        encryptedSharedPreferences?.saveIv(cipher.iv)
    }

}