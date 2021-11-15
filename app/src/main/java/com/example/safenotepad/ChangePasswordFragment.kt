package com.example.safenotepad

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.safenotepad.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var newPassword = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            changePasswordFragment = this@ChangePasswordFragment
        }

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }

        binding.saveChangeButton.setOnClickListener {
            val newPasswordString = newPassword.value
            if (newPasswordString != null) {
                sharedPreferencesDataStorage?.savePassword(newPasswordString)
                saveDataEncrypted(newPasswordString)
            }
            findNavController().navigate(ChangePasswordFragmentDirections.actionChangePasswordFragmentToNotesFragment())
            Toast.makeText(context, "Password changed!", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun saveDataEncrypted(TEXT: String) {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferencesEncrypted = context?.let {
            EncryptedSharedPreferences.create(
                "Data",
                masterKey,
                it,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        val editor = sharedPreferencesEncrypted?.edit()
        editor?.putString("Password", TEXT)
        editor?.apply()
    }
}