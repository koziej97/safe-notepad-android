package com.lukaszkoziej.safenotepad.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.lukaszkoziej.safenotepad.*
import com.lukaszkoziej.safenotepad.biometricAuthentication.BiometricAuthUtil
import com.lukaszkoziej.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import com.lukaszkoziej.safenotepad.databinding.FragmentPasswordBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PasswordFragment : Fragment() {
    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val mSharedViewModel by sharedViewModel<SharedViewModel>()
    private val encryptedSharedPreferences by inject<EncryptedSharedPreferencesDataStorage>()
    val typedPassword =  MutableLiveData<String>()

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

        val correctPassword = encryptedSharedPreferences.loadPassword()
        if (correctPassword != null) {
            mSharedViewModel.correctPassword = correctPassword
        }
        else {
            context?.let { createAlertForFirstPassword(it) }
        }

        binding.buttonPassword.setOnClickListener {
            mSharedViewModel.checkTypedPassword(typedPassword.value)
            if (mSharedViewModel.isTypedPasswordCorrect){
                findNavController().navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
            }
            else {
                Toast.makeText(context, context?.resources?.getString(R.string.wrong_password), Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonBiometric.setOnClickListener {
            openBiometricAuthDialog()
            mSharedViewModel.isBiometricAuthSucceeded.observe(viewLifecycleOwner) {
                if (mSharedViewModel.isBiometricAuthSucceeded.value == true) {
                    findNavController()
                        .navigate(PasswordFragmentDirections.actionPasswordFragmentToNotesFragment())
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createAlertForFirstPassword(context: Context){
        val firstPasswordEditText = EditText(activity)
        firstPasswordEditText.hint = context.resources.getString(R.string.type_your_password)
        firstPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
        firstPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        AlertDialog.Builder(requireActivity())
            .setTitle(context.resources.getString(R.string.set_up_your_password))
            .setView(firstPasswordEditText)
            .setPositiveButton("OK") { _, _ ->
                val correctPassword = firstPasswordEditText.text.toString()
                if (correctPassword.isNotEmpty()){
                    mSharedViewModel.saveFirstPassword(correctPassword)
                    Toast.makeText(context, context.resources.getString(R.string.type_your_password_to_get_access), Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, context.resources.getString(R.string.you_must_set_a_password), Toast.LENGTH_LONG).show()
                    createAlertForFirstPassword(context)
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun openBiometricAuthDialog(){
        val biometricAuthUtil = context?.let { BiometricAuthUtil(it, mSharedViewModel) }
        biometricAuthUtil?.showBiometricPrompt(
            activity = requireActivity() as AppCompatActivity,
            cryptoObject = null
        )
    }
}