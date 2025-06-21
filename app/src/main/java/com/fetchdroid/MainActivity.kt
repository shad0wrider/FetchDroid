package com.fetchdroid

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
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
import androidx.core.net.toUri

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

        val permsbutton = findViewById<Button>(R.id.whyperms)

        val sourcecode = findViewById<Button>(R.id.sourcecode)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedCode = prefs.getString(CODE_KEY, "")
        codeInput.setText(savedCode)

        //Check if App has ACCESS_BACKGROUND_LOCATION permission
        locationpermscheck()


        saveButton.setOnClickListener {
            val enteredCode = codeInput.text.toString().trim()
            if (enteredCode == ""){
                Toast.makeText(this,"Please Enter A CodeWord", Toast.LENGTH_SHORT).show()
            }
            else {
            prefs.edit { putString(CODE_KEY, enteredCode) }
            Toast.makeText(this, "Code saved", Toast.LENGTH_SHORT).show()
                }
        }
        sourcecode.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW,
                "https://github.com/shad0wrider/FetchDroid".toUri())
            startActivity(urlIntent)
        }

        permsbutton.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Why FetchDroid Needs these Permissions")
                .setMessage("""Ability to Access Background Location and SMS are part of FetchDroid's Core Functionality
                    read more at the Projects Privacy Policy Page                    
                """.trimMargin())
                .setPositiveButton("Read Privacy Policy"){ _, _ ->
                     val urlIntent = Intent(Intent.ACTION_VIEW,
                            "https://github.com/shad0wrider/FetchDroid/blob/main/PRIVACY.md".toUri())
                        startActivity(urlIntent)

                }
                .setNegativeButton("Cancel",null)
                .show()
        }


        // Request permissions
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

    private fun hasAlwaysLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun locationpermscheck() {
        if (hasAlwaysLocationPermission()) {
            null
        } else {
            alertbox()
        }
    }

    private fun alertbox(){
        AlertDialog.Builder(this)
            .setTitle("Enable 'Allow all the time'")
            .setMessage("To ensure FetchDroid works reliably, tap 'Permissions' > 'Location' > 'Allow all the time'.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

