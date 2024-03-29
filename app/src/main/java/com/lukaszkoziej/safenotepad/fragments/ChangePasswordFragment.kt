package com.lukaszkoziej.safenotepad.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.lukaszkoziej.safenotepad.SharedViewModel
import com.lukaszkoziej.safenotepad.databinding.FragmentChangePasswordBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val mSharedViewModel by sharedViewModel<SharedViewModel>()
    val newPassword = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.apply {
            changePasswordFragment = this@ChangePasswordFragment
        }

        binding.saveChangeButton.setOnClickListener {
            mSharedViewModel.savePassword(newPassword.value)
            findNavController().navigate(
                ChangePasswordFragmentDirections.actionChangePasswordFragmentToNotesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}