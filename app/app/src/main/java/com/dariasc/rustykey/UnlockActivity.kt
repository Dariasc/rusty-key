package com.dariasc.rustykey

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class UnlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)

        val rest = RestHandler(this)
        val biometrics = BiometricsHandler(this)

        val unlock = findViewById<Button>(R.id.unlock)
        unlock.setOnClickListener {
            biometrics.handleDecrypt(object : BiometricsHandler.Listener {
                override fun success(password: String) {
                    Log.d(TAG, "successfully decrypted password")
                    rest.dispatchPassword(password)
                }

                override fun failure(errorCode: Int, errString: CharSequence) {
                    Log.d(TAG, "authentication error. $errString ($errorCode)")
                    this@UnlockActivity.runOnUiThread {
                        Toast.makeText(this@UnlockActivity, "Authentication error", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }

        // @todo handle settings in a better way...
        val settings = findViewById<Button>(R.id.settings)
        settings.setOnClickListener {
            val alert = AlertDialog.Builder(this).setView(R.layout.modal_settings).create();
            alert.setTitle("Settings")
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "Save") { dialog, id ->
                val hostname = alert.findViewById<EditText>(R.id.hostname_field)
                if (hostname?.text.toString() != "") {
                    // @todo verify validity of the hostname
                    sharedPref.edit().putString("HOSTNAME", hostname?.text.toString()).apply()
                }

                val password = alert.findViewById<EditText>(R.id.password_field)
                if (password?.text.toString() != "") {
                    biometrics.handleEncrypt(password?.text.toString().toByteArray(), object : BiometricsHandler.Listener {
                        override fun success(password: String) {
                            Log.d(TAG, "successfully saved encrypted password")
                        }

                        override fun failure(errorCode: Int, errString: CharSequence) {
                            Log.d(TAG, "authentication error. $errString ($errorCode)")
                            this@UnlockActivity.runOnUiThread {
                                Toast.makeText(this@UnlockActivity, "Authentication error", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }
            }
            alert.show()
            val hostname = alert.findViewById<EditText>(R.id.hostname_field)
            hostname?.setText(sharedPref.getString("HOSTNAME", ""))
        }
    }

    companion object {
        private const val TAG = "BiometricsHandler"
    }

}