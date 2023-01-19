package com.example.safenotepad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.R
import com.example.safenotepad.SharedViewModel
import com.example.safenotepad.databinding.FragmentNotesBinding
import com.example.safenotepad.recyclerView.NotesListAdapter
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NotesFragment : Fragment() {

    lateinit var mAdapter: NotesListAdapter
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val mSharedViewModel by sharedViewModel<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.show()
        _binding = FragmentNotesBinding.inflate(inflater, container, false)

        mAdapter = NotesListAdapter { note ->
            val action = NotesFragmentDirections.actionNotesFragmentToEditNoteFragment(
                getString(R.string.edit_note),
                note.id
            )
            findNavController().navigate(action)
        }

        binding.lifecycleOwner = this
        binding.sharedViewModel = mSharedViewModel
        binding.notesRecyclerview.adapter = mAdapter

        mSharedViewModel.allNotes.observe(this.viewLifecycleOwner) { notes ->
            notes.let {
                mAdapter.submitList(it)
            }
        }

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
            val action = NotesFragmentDirections.actionNotesFragmentToEditNoteFragment(
                getString(R.string.add_note),
                -1
            )
            findNavController().navigate(action)
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