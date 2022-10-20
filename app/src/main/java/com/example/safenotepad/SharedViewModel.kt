package com.example.safenotepad

import androidx.lifecycle.ViewModel
import javax.crypto.Cipher

class SharedViewModel: ViewModel() {
    var salt = ByteArray(32)
    var newSalt = ByteArray(32)
    var correctPassword = String()
    var newCorrectPassword = String()

    var hashedPasswordForKey = String()
    var newHashedPasswordForKey = String()

    var noteTextShared = String()
    lateinit var cipher : Cipher
}