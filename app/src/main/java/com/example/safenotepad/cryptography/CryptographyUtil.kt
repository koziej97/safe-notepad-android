package com.example.safenotepad.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptographyUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val YOUR_SECRET_KEY_NAME = "SecretName"
    private const val KEY_SIZE = 128
    private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    private fun getOrCreateSecretKey(): SecretKey {
        // if key already exists
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(YOUR_SECRET_KEY_NAME, null)?.let { return it as SecretKey }

        // if key doesn't exist
        val paramsBuilder = KeyGenParameterSpec.Builder(
            YOUR_SECRET_KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(false)
        }

        // generating key
        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)

        val myKey = keyGenerator.generateKey()

        // setting entry password
        keyStore.setKeyEntry(YOUR_SECRET_KEY_NAME, myKey, null, null)

        return myKey
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    fun getInitializedCipherForDecryption(
        initializationVector: ByteArray? = null
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(KEY_SIZE, initializationVector)
        )
        return cipher
    }

    fun encryptData(plaintext: String, cipher: Cipher): ByteArray? {
        return cipher
            .doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
    }

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }
}