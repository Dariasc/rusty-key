package com.dariasc.rustykey

import android.content.Context
import android.util.Base64
import androidx.fragment.app.FragmentActivity
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.concurrent.thread

class RestHandler(private val activity: FragmentActivity) {

    private val client: OkHttpClient = OkHttpClient();

    fun dispatchPassword(password: String) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        thread {
            val hostname = sharedPref.getString("HOSTNAME", "")
            // @todo use some sort of url builder
            val request = Request.Builder()
                .url("http://$hostname:41337/key")
                .build()

            val response = client.newCall(request).execute()
            var pemKey = response.body()?.string()
            if (pemKey != null) {
                pemKey = pemKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                val encoded: ByteArray = Base64.decode(pemKey, Base64.DEFAULT)

                // val keySpec = RSAPublicKeySpec(BigInteger("2048"), BigInteger("65537"))
                val keySpec = X509EncodedKeySpec(encoded)
                val kf: KeyFactory = KeyFactory.getInstance("RSA")
                val pubkey: PublicKey = kf.generatePublic(keySpec)

                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(Cipher.ENCRYPT_MODE, pubkey)
                val encrypted = cipher.doFinal(password.toByteArray())

                val unlockBody = RequestBody.create(MediaType.get("text/plain"), Base64.encode(encrypted, Base64.DEFAULT))
                val unlock = Request.Builder()
                    .url("http://$hostname:41337/unlock")
                    .post(unlockBody)
                    .build()
                client.newCall(unlock).execute()
            }
        }
    }

    companion object {
        private const val TAG = "RestHandler"
    }

}