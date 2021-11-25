package com.example.safenotepad

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
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

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()

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
                val key = salt.let { it1 -> SecurityData.calculateKey(typedPasswordString, it1) }
                val hashedPassword = key.let { it1 -> SecurityData.hashFromKey(it1) }.trim()

                // Doing second hash
                val key2 = salt.let { it1 -> SecurityData.calculateKey(hashedPassword, it1) }
                val hashedPassword2 = key2.let { it1 -> SecurityData.hashFromKey(it1) }.trim()

                if (hashedPassword2 == mSharedViewModel.correctPassword){
                    mSharedViewModel.hashedPasswordForKey = hashedPassword
                    findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
                else {
                    Toast.makeText(context, "Wrong Password!", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun createAlertForFirstPassword(){
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()

        val firstPasswordEditText = EditText(activity)
        firstPasswordEditText.hint = "Type your password"

        AlertDialog.Builder(requireActivity())
            .setTitle("Set up your password")
            .setView(firstPasswordEditText)
            .setPositiveButton("OK") { _, _ ->
                var correctPassword = firstPasswordEditText.text.toString()

                if (correctPassword != ""){
                    val salt = SecurityData.generateSalt()
                    encryptedSharedPreferences?.saveSalt(salt)
                    mSharedViewModel.salt = salt
                    // Doing first hash
                    val key = salt.let { it1 -> SecurityData.calculateKey(correctPassword, it1) }
                    val hashedPassword = key.let { it1 -> SecurityData.hashFromKey(it1) }.trim()
                    // Doing second hash
                    val key2 = salt.let { it1 -> SecurityData.calculateKey(hashedPassword, it1) }
                    val hashedPassword2 = key2.let { it1 -> SecurityData.hashFromKey(it1) }.trim()
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
}