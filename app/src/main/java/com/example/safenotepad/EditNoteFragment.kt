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
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData


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

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()


        val noteTextEncrypted = sharedPreferencesDataStorage?.loadNote()
        val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
        val key = SecurityData.calculateKey(mSharedViewModel.correctPassword, mSharedViewModel.salt)
        val iv = sharedPreferencesDataStorage?.loadIv()
        val noteTextDecrypted = SecurityData.decrypt(key, noteTextEncryptedByteArray, iv!!)

        newNote.value = noteTextDecrypted

        binding.saveEditButton.setOnClickListener {
            val newNoteString = newNote.value

            if (newNoteString != null) {
                val ivNew = SecurityData.generateIv()
                sharedPreferencesDataStorage?.saveIv(ivNew)

                val newNoteEncrypted = SecurityData.encrypt(key, newNoteString, ivNew)
                val newNoteEncryptedTest = Base64.encodeToString(newNoteEncrypted, Base64.DEFAULT).trim()
                sharedPreferencesDataStorage?.saveNote(newNoteEncryptedTest)
            }

            findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}