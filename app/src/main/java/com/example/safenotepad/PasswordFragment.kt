package com.example.safenotepad

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.safenotepad.databinding.FragmentPasswordBinding

class PasswordFragment : Fragment() {

    var typedPassword =  MutableLiveData<String>()
    private var _binding: FragmentPasswordBinding? = null
    private val mSharedViewModel: SharedViewModel by activityViewModels()

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
            sharedViewModel = mSharedViewModel
        }

        val sharedPreferencesDataStorage = context?.let { SharedPreferencesDataStorage(it) }
        val encryptedSharedPreferences = context?.let { EncryptedSharedPreferencesDataStorage(it) }
        val SecurityData = SecurityData()

        var correctPassword = encryptedSharedPreferences?.loadPassword()
        if (correctPassword != null) {
            mSharedViewModel.correctPassword = correctPassword
        }
        else {
            correctPassword = "0000"
        }

        if (correctPassword == "0000"){
            Toast.makeText(context, "Default password is: 0000. Please change it in options", Toast.LENGTH_LONG).show()
        }

        binding.buttonPassword.setOnClickListener {
            val typedPasswordString = typedPassword.value

            if (typedPasswordString != null && correctPassword != "0000"){

                // Doing first hash
                val salt = encryptedSharedPreferences?.loadSalt()
                mSharedViewModel.salt = salt!!
                val key = salt.let { it1 -> SecurityData.calculateKey(typedPasswordString, it1) }
                val hashedPassword = key.let { it1 -> SecurityData.hashFromKey(it1) }.trim()

                /*
                // Doing second hash
                val key2 = salt.let { it1 -> SecurityData.calculateKey(hashedPassword, it1) }
                val hashedPassword2 = key2.let { it1 -> SecurityData.hashFromKey(it1) }.trim()
                 */

                if (hashedPassword == correctPassword){
                    //mSharedViewModel.hashedPasswordToKey = hashedPassword
                    findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
                else {
                    Toast.makeText(context, "Wrong Password!", Toast.LENGTH_LONG).show()
                }
            }
            else if (typedPasswordString != null){
                if (typedPasswordString == correctPassword){
                    findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
                else {
                    Toast.makeText(context, "Wrong Password!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Default password is: 0000. Please change it in options", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}