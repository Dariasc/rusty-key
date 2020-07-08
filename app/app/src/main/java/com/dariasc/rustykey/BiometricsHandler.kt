package com.dariasc.rustykey

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

class BiometricsHandler(private val activity: FragmentActivity) {

    private val crypto = CryptoHandler()
    val sharedPref: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)

    fun handleDecrypt(callback: Listener) {
        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { resultCipher ->
                    val encryptedData = Base64.decode(sharedPref.getString("PASSWD", ""), Base64.DEFAULT)
                    val decryptedData = resultCipher.doFinal(encryptedData)

                    Log.d(TAG, "handleDecrypt: decryption successful")
                    callback.success(String(decryptedData));
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.failure(errorCode, errString)
            }
        })

        val iv = Base64.decode(sharedPref.getString("IV", ""), Base64.DEFAULT)
        val cipher = crypto.getDecryptCipher(iv)
        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    fun handleEncrypt(data: ByteArray, callback: Listener) {
        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { resultCipher ->
                    val iv = Base64.encodeToString(resultCipher.iv, Base64.DEFAULT)
                    val encryptedData = resultCipher.doFinal(data)

                    Log.d(TAG, "handleEncrypt: iv:$iv")


                    with (sharedPref.edit()) {
                        putString("PASSWD", Base64.encodeToString(encryptedData, Base64.DEFAULT))
                        putString("IV", iv)
                        commit()
                    }
                    callback.success(String(data))
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.failure(errorCode, errString)
            }
        })

        val cipher = crypto.getEncryptCipher()
        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun biometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Rusty Key")
            .setNegativeButtonText(activity.getString(android.R.string.cancel))
            .build()
    }

    interface Listener {
        fun success(password: String)
        fun failure(errorCode: Int, errString: CharSequence)
    }

    companion object {
        private const val TAG = "BiometricsHandler"
    }

}