package com.example.safenotepad.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentEditNoteBinding

import android.util.Base64
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.example.safenotepad.cryptography.CryptographyUtil
import com.example.safenotepad.data.EncryptedSharedPreferencesDataStorage
import com.example.safenotepad.SharedViewModel
import javax.crypto.Cipher


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
                val cipher = CryptographyUtil.getInitializedCipherForEncryption()
                encryptAndSave(newNoteString, cipher)
                mSharedViewModel.noteTextShared = newNoteString
                findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun encryptAndSave(plainTextMessage: String, cipher: Cipher) {
        val encryptedMessage = CryptographyUtil.encryptData(plainTextMessage, cipher)
        // Save Encrypted Message
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        encryptedSharedPreferences?.saveNote(Base64.encodeToString(encryptedMessage, Base64.DEFAULT).trim())
        encryptedSharedPreferences?.saveIv(cipher.iv)
    }

}