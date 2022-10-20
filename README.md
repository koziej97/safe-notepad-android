# Safe Notepad Android App

Notepad Android App secured by several cryptographic techniques.

Basic application that allows you to save a note securely. You can access the app only by using the password or biometric authentication. The note is saved using several cryptographic techniques, so it's also protected from reading the note directly from app files. If the app is onPause state, the user will be automatically taken to the password screen - to save the note from leaving the application minimized.

### What I use in this app:
- AndroidKeystore
- Encrypted Shared Preferences
- Biometric authentication
- JavaX Crypto
- MVVM Architecture Pattern

### Demo:
![](demo.gif)

### Cryptography:
Password is hashed twice using PBKDF2 with HMAC-SHA1 algorithm; the first hash (not stored anywhere) is then used as a key for cipher to encrypting/decrypting the note; the second hash is stored and used to check if typed password is correct.

### Future iterations:
- [ ] more notes with use of Recycler View, 
- [ ] migrate from saving notes in Shared Preferences to Room Database,
- [ ] write some tests with jUnit5
