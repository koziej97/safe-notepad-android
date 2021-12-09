package com.example.safenotepad

import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mSharedViewModel: SharedViewModel by activityViewModels()

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
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()

        binding.saveChangeButton.setOnClickListener {
            val newPasswordString = newPassword.value

            if (newPasswordString != null) {
                // generate salt
                val salt = SecurityData.generateSalt()
                encryptedSharedPreferences?.saveSalt(salt)
                mSharedViewModel.newSalt = salt

                // Doing first hash
                val key = SecurityData.calculateKey(newPasswordString, salt)
                val hashedPassword = SecurityData.hashFromKey(key).trim()
                mSharedViewModel.newHashedPasswordForKey = hashedPassword

                // Doing second hash
                val key2 = salt.let { it1 -> SecurityData.calculateKey(hashedPassword, it1) }
                val hashedPassword2 = key2.let { it1 -> SecurityData.hashFromKey(it1) }.trim()

                //sharedPreferencesDataStorage?.savePassword(hashedPassword)
                encryptedSharedPreferences?.savePassword(hashedPassword2)
                mSharedViewModel.newCorrectPassword = hashedPassword2
            }
            findNavController().navigate(ChangePasswordFragmentDirections.actionChangePasswordFragmentToNotesFragment())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}