package com.example.safenotepad

import android.os.Bundle
import android.util.Log
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

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }
        val correctPassword = sharedPreferencesDataStorage?.loadPassword()

        if (correctPassword == "0000") {
            Toast.makeText(context, "Default password is: 0000. Please change it in options", Toast.LENGTH_LONG).show()
        }

        binding.buttonPassword.setOnClickListener {
            val typedPasswordString = typedPassword.value

            if (typedPasswordString == correctPassword){
                findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
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
}