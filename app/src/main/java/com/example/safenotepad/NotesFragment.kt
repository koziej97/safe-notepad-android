package com.example.safenotepad

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentNotesBinding


class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var noteText = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.show()

        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            notesFragment = this@NotesFragment
        }

        //Hide back arrow form ActionBar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }

        noteText.value = sharedPreferencesDataStorage?.loadNote()

        binding.buttonEditNote.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToEditNoteFragment())
        }

        binding.buttonChangePassword.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToChangePasswordFragment())
        }

        //close App when press Back Button (clear from Recent Tasks)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finishAndRemoveTask()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}