package com.example.safenotepad

import android.util.Base64
import androidx.lifecycle.*
import com.example.safenotepad.cryptography.CryptographyUtil
import com.example.safenotepad.data.database.Note
import com.example.safenotepad.data.database.NoteDao
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import javax.crypto.Cipher

class SharedViewModel(
    private val noteDao: NoteDao,
    private val sharedPreferences: EncryptedSharedPreferencesDataStorage
    ): ViewModel() {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes().asLiveData()

    fun getNoteById(id: Int): LiveData<Note> {
        return noteDao.getNote(id).asLiveData()
    }

    fun addNewNote(noteText: String){
        val newNote = Note(text = noteText)
        insertNote(newNote)
    }

    private fun insertNote(note: Note) {
        viewModelScope.launch {
            noteDao.insert(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }

    fun updateNote(id: Int, noteText: String) {
        val note = Note(id = id, text = noteText)
        updateNote(note)
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch {
            noteDao.update(note)
        }
    }

    var correctPassword = String()
    var noteTextShared = String()
    var isTypedPasswordCorrect = false
    val isBiometricAuthSucceeded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun generateSalt(): ByteArray {
        return CryptographyUtil.generateSalt()
    }

    // IMPORTANT - doing hash twice
    fun hashPassword(password: String, salt: ByteArray): String {
        val hashedPassword = hashString(password, salt)
        return hashString(hashedPassword, salt)
    }

    private fun hashString(text: String, salt: ByteArray): String {
        val key = CryptographyUtil.calculateKey(text, salt)
        return CryptographyUtil.hashFromKey(key).trim()
    }

    fun getCipherForEncryption(): Cipher {
        return CryptographyUtil.getInitializedCipherForEncryption()
    }

    fun getCipherForDecryption(iv: ByteArray?): Cipher {
        return CryptographyUtil.getInitializedCipherForDecryption(iv)
    }

    fun getEncryptedNote(note: String, cipher: Cipher): String {
        val encryptedNoteByteArray = CryptographyUtil.encryptData(note, cipher)
        return Base64.encodeToString(encryptedNoteByteArray, Base64.DEFAULT).trim()
    }

    fun getDecryptedNote(noteTextEncrypted: String?, cipher: Cipher): String{
        val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
        return CryptographyUtil.decryptData(noteTextEncryptedByteArray, cipher)
    }

    fun getDecryptedNote(noteTextEncrypted: String?, iv: ByteArray?){
        if (noteTextEncrypted != "Empty note"){
            val cipher = CryptographyUtil.getInitializedCipherForDecryption(iv)
            val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
            val decryptedMessage =
                CryptographyUtil.decryptData(noteTextEncryptedByteArray, cipher)
            noteTextShared = decryptedMessage
        }
        else {
            noteTextShared = noteTextEncrypted
        }
    }

    fun isEntryValid(text: String): Boolean {
        if (text.isBlank()){
            return false
        }
        return true
    }
}