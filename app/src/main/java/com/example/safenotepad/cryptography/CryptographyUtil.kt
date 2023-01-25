package com.example.safenotepad.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

object CryptographyUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val YOUR_SECRET_KEY_NAME = "SecretName"
    private const val KEY_SIZE = 128
    private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"

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

    fun calculateKey(password: String, salt: ByteArray): SecretKey {
        return SecretKeyFactory.getInstance(ALGORITHM)
            .generateSecret(
                PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    10000,
                    256
                )
            )
    }

    fun hashFromKey(key: SecretKey): String {
        return Base64.encodeToString(key.encoded, Base64.DEFAULT).trim()
    }

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return salt
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        // if key already exists
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(YOUR_SECRET_KEY_NAME, null)?.let {
            return it as SecretKey
        }
        // if key doesn't exist
        val keyGenParams = buildKeyGenParameter()
        val keyGenerator = getKeyGenerator()
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    private fun buildKeyGenParameter(): KeyGenParameterSpec {
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
        return paramsBuilder.build()
    }

    private fun getKeyGenerator(): KeyGenerator {
        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
    }
}