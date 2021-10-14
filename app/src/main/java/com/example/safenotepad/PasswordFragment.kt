package com.example.safenotepad

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentPasswordBinding

class PasswordFragment : Fragment() {

    var typedPassword =  MutableLiveData<String>()
    private var _binding: FragmentPasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.apply {
            passwordFragment = this@PasswordFragment
        }

        binding.buttonPassword.setOnClickListener {
            val typedPasswordString = typedPassword.value

            val correctPassword = loadData()

            if (typedPasswordString == correctPassword){
                findNavController().navigate(R.id.action_PasswordFragment_to_NotesFragment)
            }
            else {
                Toast.makeText(context, "Wrong Password!", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun loadData(): String? {
        val sharedPreferences =
            context?.getSharedPreferences("Shared Preferences", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences?.contains("Password") == false){
            return "0000"
        }
        return sharedPreferences?.getString("Password", "")
    }

}