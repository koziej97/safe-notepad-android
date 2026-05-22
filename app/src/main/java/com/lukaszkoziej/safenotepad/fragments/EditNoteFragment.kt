package com.lukaszkoziej.safenotepad.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lukaszkoziej.safenotepad.R
import com.lukaszkoziej.safenotepad.SharedViewModel
import com.lukaszkoziej.safenotepad.data.database.Note
import com.lukaszkoziej.safenotepad.databinding.FragmentEditNoteBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditNoteFragment : Fragment() {
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private var originalNoteContent: String = ""

    lateinit var note: Note
    private val navigationArgs: EditNoteFragmentArgs by navArgs()

    private val mSharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noteId = navigationArgs.noteId
        if (noteId > 0) {
            mSharedViewModel.getNoteById(noteId).observe(this.viewLifecycleOwner) { selectedNote ->
                val decryptedNoteText = mSharedViewModel.getDecryptedNote(selectedNote)
                note = Note(id = noteId, text = decryptedNoteText)
                bindEditNote(note)
                originalNoteContent = decryptedNoteText
            }
        } else {
            bindAddNote()
            originalNoteContent = ""
        }

        requireActivity().addMenuProvider(EditNoteMenuProvider(), viewLifecycleOwner)
        setupOnBackPressedCallback()

        // Ensure bottom buttons sit above the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(bottom = sysBars.bottom)
            insets
        }
    }

    private fun setupOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleNavigationAttempt()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleNavigationAttempt() {
        if (isNoteContentChanged()) {
            showUnsavedChangesDialog(requireContext())
        } else {
            findNavController().navigateUp()
        }
    }

    private fun bindEditNote(note: Note) {
        _binding?.apply {
            editNoteFragment = this@EditNoteFragment
            noteEditText.text = Editable.Factory.getInstance().newEditable(note.text)
        }

        binding.saveEditButton.setOnClickListener {
            updateNote()
        }

        binding.deleteButton.setOnClickListener {
            createConfirmAlertForDeleteButton(requireContext())
        }
    }

    private fun updateNote() {
        if (isEntryValid()) {
            val newContent = binding.noteEditText.text.toString()
            mSharedViewModel.updateNote(note.id, newContent)
            originalNoteContent = newContent
            val action = EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment()
            findNavController().navigate(action)
        }
    }

    private fun isNoteContentChanged(): Boolean {
        return binding.noteEditText.text.toString().trim() != originalNoteContent.trim()
    }


    private fun bindAddNote() {
        _binding?.apply {
            editNoteFragment = this@EditNoteFragment
            deleteButton.isEnabled = false
        }

        binding.saveEditButton.setOnClickListener {
            addNewNote()
        }

        binding.deleteButton.setOnClickListener {
            createConfirmAlertForDeleteButton(requireContext())
        }
    }

    private fun addNewNote() {
        if (isEntryValid()) {
            val newContent = binding.noteEditText.text.toString()
            mSharedViewModel.addNewNote(newContent)
            originalNoteContent = newContent
            val action = EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment()
            findNavController().navigate(action)
        }
    }

    private fun isEntryValid(): Boolean {
        return mSharedViewModel.isEntryValid(binding.noteEditText.text.toString())
    }

    private fun deleteNote() {
        mSharedViewModel.deleteNote(note)
        findNavController().navigateUp()
    }

    private fun createConfirmAlertForDeleteButton(context: Context){
        AlertDialog.Builder(requireActivity())
            .setTitle(context.resources.getString(R.string.confirm_delete_button))
            .setPositiveButton(context.resources.getString(R.string.yes)) { _, _ ->
                Toast.makeText(context, context.resources.getString(R.string.deleting_note), Toast.LENGTH_LONG).show()
                deleteNote()
            }
            .setNegativeButton(context.resources.getString(R.string.no), null)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showUnsavedChangesDialog(context: Context) {
        AlertDialog.Builder(requireContext())
            .setTitle(context.resources.getString(R.string.unsaved_changes))
            .setMessage(context.resources.getString(R.string.save_your_changes_before_exiting))
            .setPositiveButton(context.resources.getString(R.string.save)) { _, _ ->
                if (isEntryValid()) {
                    if (::note.isInitialized && navigationArgs.noteId > 0) {
                        updateNote()
                    } else {
                        addNewNote()
                    }
                } else {
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton(context.resources.getString(R.string.discard)) { _, _ ->
                findNavController().navigateUp()
            }
            .setNeutralButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class EditNoteMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

        override fun onPrepareMenu(menu: Menu) {
            menu.clear()
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == android.R.id.home) {
                handleNavigationAttempt()
                return true
            }
            return false
        }
    }
}
