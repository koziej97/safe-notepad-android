package com.example.safenotepad.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentEditNoteBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.example.safenotepad.R
import com.example.safenotepad.SafeNotepadApplication
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.SharedViewModelFactory
import com.example.safenotepad.data.database.Note
import javax.crypto.Cipher

class EditNoteFragment : Fragment() {
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var note: Note

    private val mSharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (activity?.application as SafeNotepadApplication).database.noteDao()
        )
    }

    private val navigationArgs: EditNoteFragmentArgs by navArgs()

    var newNote = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        newNote.value = mSharedViewModel.noteTextShared
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noteId = navigationArgs.noteId
        if (noteId > 0) {
            mSharedViewModel.getNoteById(noteId).observe(this.viewLifecycleOwner) { selectedNote ->
                note = selectedNote
                bind(note)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bind(note: Note) {
        _binding?.apply {
            editNoteFragment = this@EditNoteFragment
            noteEditText.text = Editable.Factory.getInstance().newEditable(note.text)
        }

        binding.saveEditButton.setOnClickListener {
            saveNote()
        }

        binding.deleteButton.setOnClickListener {
            createConfirmAlertForDeleteButton(requireContext())
        }
    }

    private fun saveNote(){
        val newNoteString = newNote.value
        if (newNoteString != null) {
            val cipher = mSharedViewModel.getCipherForEncryption()
            encryptAndSave(newNoteString, cipher)
            mSharedViewModel.noteTextShared = newNoteString
            findNavController().navigate(
                EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
        }
    }

    private fun encryptAndSave(note: String, cipher: Cipher) {
        val encryptedNote = mSharedViewModel.getEncryptedNote(note, cipher)
        val encryptedSharedPreferences = EncryptedSharedPreferencesDataStorage(requireContext())
        encryptedSharedPreferences.saveNote(encryptedNote)
        encryptedSharedPreferences.saveIv(cipher.iv)
    }

    private fun createConfirmAlertForDeleteButton(context: Context){
        AlertDialog.Builder(requireActivity())
            .setTitle(context.resources.getString(R.string.confirm_delete_button))
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(context, "Deleting note...", Toast.LENGTH_LONG).show()
                findNavController().navigate(
                    EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
            }
            .setNegativeButton("No", null)
            .setCancelable(false)
            .create()
            .show()
    }

}