package com.example.safenotepad

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class SharedViewModel: ViewModel() {
    var salt = ByteArray(32)
    var newSalt = ByteArray(32)
    var correctPassword = String()
    var newCorrectPassword = String()

    var hashedPasswordForKey = String()
    var newHashedPasswordForKey = String()
}