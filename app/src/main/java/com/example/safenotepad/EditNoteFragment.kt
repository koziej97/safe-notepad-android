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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData


class EditNoteFragment : Fragment() {
    private var _binding: FragmentEditNoteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        newNote.value = loadData()

        binding.saveEditButton.setOnClickListener {
            val newNoteString = newNote.value
            if (newNoteString != null) {
                saveData(newNoteString)
            }

            findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNotesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun saveData(TEXT: String) {
        val sharedPreferences = context?.getSharedPreferences("Shared Preferences",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("Note", TEXT)
        editor?.apply()
    }

    fun loadData(): String? {
        val sharedPreferences =
            context?.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences?.contains("Note") == false){
            return "Empty note"
        }
        return sharedPreferences?.getString("Note", "")
    }
}