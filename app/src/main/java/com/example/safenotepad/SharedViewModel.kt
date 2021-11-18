package com.example.safenotepad

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.crypto.SecretKey

class SharedViewModel: ViewModel() {
    var salt = ByteArray(32)
    var correctPassword = String()
    var hashedPasswordToKey = String()
}