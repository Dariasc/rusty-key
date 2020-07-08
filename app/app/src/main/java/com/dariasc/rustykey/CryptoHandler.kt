package com.dariasc.rustykey

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoHandler {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
    private val key = fetchKey();

    private fun fetchKey(): Key {
        keyStore.getKey(KEY_NAME, null)?.let { return it }
        return createKey();
    }

    private fun createKey(): SecretKey {
        val provider = "AndroidKeyStore"
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, provider)

        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME, purposes)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setUserAuthenticationRequired(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    fun getDecryptCipher(iv: ByteArray): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv)) }

    fun getEncryptCipher(): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.ENCRYPT_MODE, key) }

    companion object {
        private const val TAG = "CryptoHandler"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_NAME = "RUSTY_KEY"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private fun keyTransformation() = listOf(ALGORITHM, BLOCK_MODE, PADDING).joinToString(separator = "/")
    }

}