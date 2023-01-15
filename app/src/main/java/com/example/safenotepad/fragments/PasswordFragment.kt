package com.example.safenotepad.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.*
import com.example.safenotepad.biometricAuthentication.BiometricAuthUtil
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import com.example.safenotepad.databinding.FragmentPasswordBinding

class PasswordFragment : Fragment() {
    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val mSharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (activity?.application as SafeNotepadApplication).database.noteDao()
        )
    }
    val typedPassword =  MutableLiveData<String>()

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
        val encryptedSharedPreferences = EncryptedSharedPreferencesDataStorage(requireContext())
        val correctPassword = encryptedSharedPreferences.loadPassword()
        if (correctPassword != null) {
            mSharedViewModel.correctPassword = correctPassword
        }
        else {
            createAlertForFirstPassword(encryptedSharedPreferences)
        }

        binding.buttonPassword.setOnClickListener {
            checkTypedPassword(encryptedSharedPreferences)
            if (mSharedViewModel.isTypedPasswordCorrect){
                findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
            }
            else {
                Toast.makeText(context, "Wrong Password!", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonBiometric.setOnClickListener {
            openBiometricAuthDialog(encryptedSharedPreferences)
            mSharedViewModel.isBiometricAuthSucceeded.observe(viewLifecycleOwner) {
                if (mSharedViewModel.isBiometricAuthSucceeded.value == true) {
                    findNavController()
                        .navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createAlertForFirstPassword(encryptedSharedPreferences: EncryptedSharedPreferencesDataStorage){
        val firstPasswordEditText = EditText(activity)
        firstPasswordEditText.hint = "Type your password"
        firstPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        AlertDialog.Builder(requireActivity())
            .setTitle("Set up your password")
            .setView(firstPasswordEditText)
            .setPositiveButton("OK") { _, _ ->
                val correctPassword = firstPasswordEditText.text.toString()
                if (correctPassword != ""){
                    saveNewPassword(correctPassword, encryptedSharedPreferences)
                    Toast.makeText(context, "Now type your set password to get access", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, "You MUST set a password!", Toast.LENGTH_LONG).show()
                    createAlertForFirstPassword(encryptedSharedPreferences)
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun saveNewPassword(correctPassword: String,
                                encryptedSharedPreferences: EncryptedSharedPreferencesDataStorage
    ){
        val salt = mSharedViewModel.generateSalt()
        encryptedSharedPreferences.saveSalt(salt)
        val hashedPassword = mSharedViewModel.hashPassword(correctPassword, salt)
        encryptedSharedPreferences.savePassword(hashedPassword)
        mSharedViewModel.correctPassword = hashedPassword
    }

    private fun checkTypedPassword(encryptedSharedPreferences: EncryptedSharedPreferencesDataStorage){
        val typedPasswordString = typedPassword.value
        if (typedPasswordString != null){
            val salt = encryptedSharedPreferences.loadSalt()
            val hashedPassword =
                salt?.let { mSharedViewModel.hashPassword(typedPasswordString, it) }
            if (hashedPassword == mSharedViewModel.correctPassword){
                val noteTextEncrypted = encryptedSharedPreferences.loadNote()
                val iv = encryptedSharedPreferences.loadIv()
                mSharedViewModel.getDecryptedNote(noteTextEncrypted, iv)
                mSharedViewModel.isTypedPasswordCorrect = true
            }
        }
    }

    private fun openBiometricAuthDialog(encryptedSharedPreferences: EncryptedSharedPreferencesDataStorage){
        val biometricAuthUtil = BiometricAuthUtil(requireContext(), mSharedViewModel)
        val noteTextEncrypted = encryptedSharedPreferences.loadNote()
        if (noteTextEncrypted != "Empty note"){
            val iv = encryptedSharedPreferences.loadIv()
            val cryptoObject = BiometricPrompt.CryptoObject(
                mSharedViewModel.getCipherForDecryption(iv)
            )
            biometricAuthUtil.showBiometricPrompt(
                activity = requireActivity() as AppCompatActivity,
                cryptoObject = cryptoObject
            )
        }
        else {
            mSharedViewModel.noteTextShared = noteTextEncrypted
            biometricAuthUtil.showBiometricPrompt(
                activity = requireActivity() as AppCompatActivity,
                cryptoObject = null
            )
        }
    }
}