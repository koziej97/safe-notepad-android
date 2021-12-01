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

                //change cipher for note
                val noteTextEncrypted = encryptedSharedPreferences?.loadNote()
                val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
                val oldKey = SecurityData.calculateKey(mSharedViewModel.hashedPasswordForKey, mSharedViewModel.salt)
                val iv = encryptedSharedPreferences?.loadIv()
                val noteTextDecrypted = SecurityData.decrypt(oldKey, noteTextEncryptedByteArray, iv!!)

                //tworze nowy klucz
                val newKey = SecurityData.calculateKey(mSharedViewModel.newHashedPasswordForKey, mSharedViewModel.newSalt)
                val newNoteText = SecurityData.encrypt(newKey, noteTextDecrypted, iv)
                val newNoteEncryptedText = Base64.encodeToString(newNoteText, Base64.DEFAULT).trim()
                encryptedSharedPreferences.saveNote(newNoteEncryptedText)

            }
            //findNavController().navigate(ChangePasswordFragmentDirections.actionChangePasswordFragmentToNotesFragment())
            Toast.makeText(context, "Password changed! Restart App", Toast.LENGTH_LONG).show()
            // close app
            activity?.finishAndRemoveTask()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}