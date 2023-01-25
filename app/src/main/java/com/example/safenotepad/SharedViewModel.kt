package com.example.safenotepad

import android.util.Base64
import androidx.lifecycle.*
import com.example.safenotepad.cryptography.CryptographyUtil
import com.example.safenotepad.data.database.Note
import com.example.safenotepad.data.database.NoteDao
import com.example.safenotepad.data.sharedPreferences.EncryptedSharedPreferencesDataStorage
import kotlinx.coroutines.launch
import javax.crypto.Cipher

class SharedViewModel(
    private val noteDao: NoteDao,
    private val sharedPreferences: EncryptedSharedPreferencesDataStorage
    ): ViewModel() {

    var correctPassword = String()
    var isTypedPasswordCorrect = false
    val isBiometricAuthSucceeded: MutableLiveData<Boolean> = MutableLiveData(false)

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes().asLiveData()

    fun isEntryValid(text: String): Boolean {
        if (text.isBlank()){
            return false
        }
        return true
    }

    fun checkTypedPassword(typedPasswordString: String?){
        if (typedPasswordString != null){
            val salt = sharedPreferences.loadSalt()
            val hashedPassword =
                salt?.let { hashPassword(typedPasswordString, it) }
            if (hashedPassword == correctPassword){
                isTypedPasswordCorrect = true
            }
        }
    }

    fun saveFirstPassword(newPassword: String) {
        val hashedPassword = savePassword(newPassword)
        if (hashedPassword != null) {
            correctPassword = hashedPassword
        }
    }

    fun savePassword(newPasswordString: String?): String? {
        if (newPasswordString != null) {
            val salt = generateSalt()
            sharedPreferences.saveSalt(salt)
            val hashedPassword = hashPassword(newPasswordString, salt)
            sharedPreferences.savePassword(hashedPassword)
            return hashedPassword
        }
        return null
    }

    fun decryptAllNotes(allNotes: List<Note>): List<Note> {
        val decryptedNotes = mutableListOf<Note>()
        allNotes.forEach { note ->
            val decryptedNoteText = getDecryptedNote(note)
            val decryptedNote = Note(id = note.id, text = decryptedNoteText)
            decryptedNotes.add(decryptedNote)
        }
        return decryptedNotes
    }

    fun getDecryptedNote(note: Note): String {
        val iv = sharedPreferences.loadIv(note.id.toString())
        val cipher = getCipherForDecryption(iv)
        return getDecryptedNote(note.text, cipher)
    }

    fun getNoteById(id: Int): LiveData<Note> {
        return noteDao.getNote(id).asLiveData()
    }

    fun addNewNote(noteText: String){
        val cipher = getCipherForEncryption()
        val encryptedNoteText = getEncryptedNote(noteText, cipher)
        val newNote = Note(text = encryptedNoteText)
        insertNote(newNote, cipher.iv)
    }

    private fun insertNote(note: Note, iv: ByteArray) {
        viewModelScope.launch {
            val noteId = noteDao.insert(note)
            sharedPreferences.saveIv(noteId.toString(), iv)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
            sharedPreferences.removeIv(note.id.toString())
        }
    }

    fun updateNote(id: Int, noteText: String) {
        val cipher = getCipherForEncryption()
        val encryptedNoteText = getEncryptedNote(noteText, cipher)
        val note = Note(id = id, text = encryptedNoteText)
        updateNote(note, cipher.iv)
    }

    private fun updateNote(note: Note, iv: ByteArray) {
        viewModelScope.launch {
            noteDao.update(note)
            sharedPreferences.saveIv(note.id.toString(), iv)
        }
    }

    private fun generateSalt(): ByteArray {
        return CryptographyUtil.generateSalt()
    }

    // IMPORTANT - doing hash twice
    private fun hashPassword(password: String, salt: ByteArray): String {
        val hashedPassword = hashString(password, salt)
        return hashString(hashedPassword, salt)
    }

    private fun hashString(text: String, salt: ByteArray): String {
        val key = CryptographyUtil.calculateKey(text, salt)
        return CryptographyUtil.hashFromKey(key).trim()
    }

    private fun getCipherForEncryption(): Cipher {
        return CryptographyUtil.getInitializedCipherForEncryption()
    }

    private fun getCipherForDecryption(iv: ByteArray?): Cipher {
        return CryptographyUtil.getInitializedCipherForDecryption(iv)
    }

    private fun getEncryptedNote(note: String, cipher: Cipher): String {
        val encryptedNoteByteArray = CryptographyUtil.encryptData(note, cipher)
        return Base64.encodeToString(encryptedNoteByteArray, Base64.DEFAULT).trim()
    }

    private fun getDecryptedNote(noteTextEncrypted: String?, cipher: Cipher): String{
        val noteTextEncryptedByteArray = Base64.decode(noteTextEncrypted, Base64.DEFAULT)
        return CryptographyUtil.decryptData(noteTextEncryptedByteArray, cipher)
    }
}