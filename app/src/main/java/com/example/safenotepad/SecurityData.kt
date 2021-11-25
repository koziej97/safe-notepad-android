package com.example.safenotepad

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec

class SecurityData {
    fun encrypt(key: SecretKey, text: String, iv: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        val data = cipher.doFinal(text.toByteArray())
        return data
    }

    fun decrypt(key: SecretKey, text: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(text))
    }

    fun calculateKey(password: String, salt: ByteArray): SecretKey {
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(
                PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    10000,
                    256)
            )
        return key
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

    fun generateIv(): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(cipher.blockSize)
        val random = SecureRandom()
        random.nextBytes(iv)
        return iv
    }
}