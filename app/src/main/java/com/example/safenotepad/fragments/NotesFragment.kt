package com.example.safenotepad.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.databinding.FragmentNotesBinding

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val mSharedViewModel: SharedViewModel by activityViewModels()
    var noteText = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.show()
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteText.value = mSharedViewModel.noteTextShared
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.apply {
            notesFragment = this@NotesFragment
        }

        //Hide back arrow from ActionBar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        _binding?.note?.movementMethod = ScrollingMovementMethod()

        binding.noteCard.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToEditNoteFragment())
        }

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