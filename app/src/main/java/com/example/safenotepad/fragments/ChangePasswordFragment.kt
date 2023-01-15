package com.example.safenotepad.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val mSharedViewModel: SharedViewModel by activityViewModels()
    val newPassword = MutableLiveData<String>()

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

        binding.saveChangeButton.setOnClickListener {
            changePassword()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changePassword(){
        val encryptedSharedPreferences = EncryptedSharedPreferencesDataStorage(requireContext())
        val newPasswordString = newPassword.value
        if (newPasswordString != null) {
            val salt = mSharedViewModel.generateSalt()
            encryptedSharedPreferences.saveSalt(salt)
            val hashedPassword = mSharedViewModel.hashPassword(newPasswordString, salt)
            encryptedSharedPreferences.savePassword(hashedPassword)
        }
        findNavController().navigate(
            ChangePasswordFragmentDirections.actionChangePasswordFragmentToNotesFragment())
    }
}