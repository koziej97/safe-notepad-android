package com.example.safenotepad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.R
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.data.database.Note
import com.example.safenotepad.databinding.FragmentNotesBinding
import com.example.safenotepad.recyclerView.NotesItemClickListener
import com.example.safenotepad.recyclerView.NotesListAdapter

class NotesFragment : Fragment(), NotesItemClickListener {

    lateinit var mAdapter: NotesListAdapter
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
//        noteText.value = mSharedViewModel.noteTextShared

        mAdapter = NotesListAdapter(this)

        binding.lifecycleOwner = this
        binding.sharedViewModel = mSharedViewModel
        binding.notesRecyclerview.adapter = mAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.apply {
            notesFragment = this@NotesFragment
        }

        //Hide back arrow from ActionBar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.floatingButton.setOnClickListener {
            Toast.makeText(context, "Clicked floating button", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_NotesFragment_to_editNoteFragment)
        }

        //close App when press Back Button (clear from Recent Tasks)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finishAndRemoveTask()
        }
    }

    override fun chosenNote(note: Note) {
        super.chosenNote(note)
        val bundle = Bundle()
        bundle.putInt("noteId", note.id)
        findNavController().navigate(R.id.action_NotesFragment_to_editNoteFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}