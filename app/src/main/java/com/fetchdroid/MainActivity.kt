package com.fetchdroid

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.fetchdroid.R
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    // Biometric Auth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // Code Input
    private lateinit var codeInput: EditText
    private lateinit var saveButton: Button

    private val PREFS_NAME = "TrackerPrefs"
    private val CODE_KEY = "codeWord"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Only show the content if auth succeeds
                    showMainUI()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Auth error: $errString", Toast.LENGTH_SHORT).show()
                    finish() // Exit app on error
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to access FetchDroid")
            .setSubtitle("Use fingerprint, PIN, or face unlock")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showMainUI() {
        setContentView(R.layout.activity_main)

        codeInput = findViewById(R.id.codeInput)
        saveButton = findViewById(R.id.saveButton)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedCode = prefs.getString(CODE_KEY, "")
        codeInput.setText(savedCode)

        saveButton.setOnClickListener {
            val enteredCode = codeInput.text.toString().trim()
            prefs.edit { putString(CODE_KEY, enteredCode) }
            Toast.makeText(this, "Code saved", Toast.LENGTH_SHORT).show()
        }

        // Request permissions (optional since you're granting manually)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ),
            1
        )
    }
}

