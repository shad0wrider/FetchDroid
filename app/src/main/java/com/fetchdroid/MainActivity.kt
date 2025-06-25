//Built by ❤️ https://github.com/shad0wrider

package com.fetchdroid

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
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
    private lateinit var ringInput: EditText
    private lateinit var ringTime: SeekBar
    private lateinit var ringTimeBar: TextView
    private lateinit var saveButton: Button
    private lateinit var testRing: Button

    private val PREFS_NAME = "TrackerPrefs"
    private val CODE_KEY = "codeWord"
    private val RING_KEY = "ringWord"
    private val RING_TIME_KEY = "ringTime"



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


    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    private fun showMainUI() {

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

        setContentView(R.layout.activity_main)

        codeInput = findViewById(R.id.codeInput)
        ringInput = findViewById<EditText>(R.id.ringInput)
        saveButton = findViewById(R.id.saveButton)
        ringTime = findViewById(R.id.ringTime)
        ringTimeBar = findViewById(R.id.ringTimeBar)
        testRing = findViewById<Button>(R.id.testRing)

        val tmptext = ringcal().toString()

        ringTimeBar.text = "Ring For: ${tmptext} Seconds"


        val permsbutton = findViewById<Button>(R.id.whyperms)

        var isPasswordVisible = false

        var isRingVisible = false

        val ringVal = ringcal()

        val isRinging = false

        val sourcecode = findViewById<Button>(R.id.sourcecode)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedCode = prefs.getString(CODE_KEY, "")
        val ringCode = prefs.getString(RING_KEY, "")
        codeInput.setText(savedCode)
        ringInput.setText(ringCode)

        //Check if App has ACCESS_BACKGROUND_LOCATION permission
        locationpermscheck()

        //Listener for onClick Savebutton
        saveButton.setOnClickListener {
            val enteredCode = codeInput.text.toString().trim()
            val enteredringCode = ringInput.text.toString().trim()
            if (enteredCode == "" || enteredringCode == ""){
                Toast.makeText(this,"Please Enter A CodeWord", Toast.LENGTH_SHORT).show()
            }
            else {
            prefs.edit { putString(CODE_KEY, enteredCode) }
            prefs.edit {putString(RING_KEY, enteredringCode) }
            prefs.edit {putInt(RING_TIME_KEY, ringVal)}
                codeInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ringInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                codeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible_eye, 0)
                ringInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible_eye, 0)
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
                }
        }
        //Sourcecode setOnClick Listener
        sourcecode.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW,
                "https://github.com/shad0wrider/FetchDroid".toUri())
            startActivity(urlIntent)
        }
        // permsbutton setonClick Listener
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

        //Touch Listener for Location code word password
        codeInput.setOnTouchListener { v, event ->
            val DRAWABLE_END = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = codeInput.compoundDrawables[DRAWABLE_END]
                if (drawable != null) {
                    val bounds = drawable.bounds
                    val x = event.rawX.toInt() - codeInput.right + codeInput.paddingEnd
                    if (x >= -bounds.width()) {
                        isPasswordVisible = !isPasswordVisible
                        if (isPasswordVisible) {
                            codeInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            codeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visible_eye, 0)
                        } else {
                            codeInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            codeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible_eye, 0)
                        }
                        // Maintain cursor at end
                        codeInput.setSelection(codeInput.text.length)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        //Touch Listener for Ring word Password
        ringInput.setOnTouchListener { v,event ->
            val DRAWABLE_RING_END = 2
            if (event.action == MotionEvent.ACTION_UP){
                val drawablering = ringInput.compoundDrawables[DRAWABLE_RING_END]
                if (drawablering != null){
                    val ringbounds = drawablering.bounds
                    val ringx = event.rawX.toInt() - ringInput.right + ringInput.paddingEnd
                    if (ringx >= -ringbounds.width()) {
                        isRingVisible = !isRingVisible
                        if (isRingVisible){
                            ringInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            ringInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visible_eye, 0)
                        } else {
                            ringInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            ringInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.invisible_eye, 0)
                        }
                        //Maintain Cursor at end
                        ringInput.setSelection(ringInput.text.length)
                        return@setOnTouchListener true
                    }
                }

            }
            false
        }

        //Change Listener for ring time Seekbar
        ringTime.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val tmpval = ringcal().toString()
                ringTimeBar.text = "Ring For: ${tmpval} Seconds"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                null
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                null
            }

        })

        //Test Ring Touch Listener
        val ringer = Ringer(this)
        testRing.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Button pressed
                    ringer.startRinging()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Button released or finger dragged off
                    ringer.stopRinging()
                    true
                }
                else -> false
            }
        }


        // Request permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
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

    private fun ringcal():Int{

        var ringit = 0

        if (ringTime.progress ==0){
            ringit =  20
        }
        else if (ringTime.progress ==1){
            ringit =  30
        }
        else if (ringTime.progress ==2){
            ringit =  40
        }
        else if (ringTime.progress ==3){
            ringit =  50
        }
        else if(ringTime.progress ==4){
            ringit =  60
        }
        return ringit
    }


    private fun alertbox(){
        AlertDialog.Builder(this)
            .setTitle("Enable 'Allow all the time'")
            .setMessage("To ensure FetchDroid works Properly, tap 'Permissions' > 'Location' > 'Allow all the time'.")
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



