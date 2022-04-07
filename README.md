# Safe Notepad Android App

Notepad Android App secured by several cryptographic techniques - still in progress.

### Done ✓

- [x] Working basic App  
- [x] correct navigation beetwen Fragments
  - [x] close App by back button from NotesFragment
  - [x] don't go back to PasswordFragment
  - [x] close app for restart after changing password
- [x] make a separate class to handle Encrypted Shared Preferences (Shared Preferences also done, but finaly not in use)
- [x] secure: password is hashed twice; the first hash (not store in Encrypted Shared Preferences) is then used as a key for cipher used for Note; the second hash is stored and used to check if typed password is correct

### TODO
- [ ] clean up the mess (the code is disaster, make functions, move them to Shared View Model, etc)
- [x] add biometric log in option
- [ ] maybe more Notes? (Recycler View, migrate from Shared Preferences to Room Database)
