/*
 * FetchDroid - A privacy-first SMS-based phone locator
 * Copyright (C) 2025 Advait M
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

//Built with ❤️ By https://github.com/shad0wrider

package com.fetchdroid

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64 as ringevents
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import com.fetchdroid.Ringer
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {

    // Biometric Auth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    //Welcome UI
    private lateinit var welcomeimagebox: ImageView
    private lateinit var welcomeprevious: Button
    private lateinit var welcomenext: Button
    private lateinit var welcometopbanner: TextView

    // Code Input
    private lateinit var codeInput: EditText
    private lateinit var ringInput: EditText
    private lateinit var ringTime: SeekBar
    private lateinit var ringTimeBar: TextView
    private lateinit var saveButton: Button
    private lateinit var testRing: Button
    private lateinit var stopRing: Button

    private val PREFS_NAME = "TrackerPrefs"
    private val CODE_KEY = "codeWord"
    private val RING_KEY = "ringWord"
    private val RING_TIME_KEY = "ringTime"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tmprefs = getSharedPreferences("TrackerPrefs",MODE_PRIVATE)
        val setupdone = tmprefs.getString("setupok","0")

        if (setupdone == "1") {
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(
                this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Only show the content if auth succeeds
                        showMainUI()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            applicationContext,
                            "Auth error: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Exit app on error
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            applicationContext,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
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
        else if (setupdone == "0"){
            showWelcomeUI()
        }
    }

    @SuppressLint("ResourceType")
    private fun showWelcomeUI(){
        setContentView(R.layout.activity_welcome)

        val setupPrefs = getSharedPreferences("TrackerPrefs",MODE_PRIVATE)


        var curPage = ""

        val screendensity = resources.displayMetrics.density

        var dark1 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_location_dark))
        var dark2 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_ringing_dark))
        var dark3 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_save_dark))

        var light1 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_location_light))
        var light2 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_ringing_light))
        var light3 = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_save_light))

        var batteryperms = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_battery_perms_dark))
        var locationperms = BitmapFactory.decodeStream(resources.openRawResource(R.raw.welcome_location_perms_dark))

        welcomenext = findViewById<Button>(R.id.welcomenext)
        welcomeprevious = findViewById<Button>(R.id.welcomeprevious)
        welcometopbanner = findViewById<TextView>(R.id.welcometopbanner)

        welcomeimagebox = findViewById<ImageView>(R.id.welcomeimagebox)
        welcomeimagebox.setImageBitmap(batteryperms)

        welcomenext.setOnClickListener {
            if (curPage ==""){
                welcomeimagebox.setImageBitmap(locationperms)
                welcometopbanner.text = "Location\nPermission"
                curPage = "2"
                welcomenext.text = "Next"
            }
            else if (curPage =="2") {
                welcomeimagebox.setImageBitmap(dark1)
                welcometopbanner.setPadding(
                    welcometopbanner.paddingLeft,
                    (screendensity * 75).toInt(),
                    welcometopbanner.paddingRight,
                    welcometopbanner.paddingBottom
                )
                welcometopbanner.text = "Location Feature"
                curPage = "3"
            }

            else if (curPage =="3"){
                welcomeimagebox.setImageBitmap(dark2)
                welcometopbanner.text = "Ring Feature"
                curPage = "4"
            }
            else if (curPage =="4"){
                welcomeimagebox.setImageBitmap(dark3)
                welcometopbanner.text = "Save Settings"
                curPage = "5"
                welcomenext.text = "Done"
            }

            else if (curPage =="5"){
                setupPrefs.edit { putString("setupok","1")}
                showMainUI()
            }

            else if (curPage == "1"){
                welcomeimagebox.setImageBitmap(locationperms)
                welcometopbanner.text = "Location\nPermission"
                curPage = "2"

            }


        }

        welcomeprevious.setOnClickListener {
            if (curPage =="5"){
                welcomeimagebox.setImageBitmap(dark2)
                welcometopbanner.text = "Ring Feature"
                curPage = "4"
                welcomenext.text = "Next"
            }
            else if (curPage == "4"){
                welcomeimagebox.setImageBitmap(dark1)
                welcometopbanner.text = "Location Feature"
                curPage = "3"
                welcomenext.text = "Next"
            }

            else if (curPage =="3"){
                welcomeimagebox.setImageBitmap(locationperms)
                welcometopbanner.setPadding(
                    welcometopbanner.paddingLeft,
                    (screendensity * 55).toInt(),
                    welcometopbanner.paddingRight,
                    welcometopbanner.paddingBottom
                )
                welcometopbanner.text = "Location\nPermission"
                curPage = "2"
            }
            else if (curPage =="2"){
                welcomeimagebox.setImageBitmap(batteryperms)
                welcometopbanner.text = "Battery\nOptimization"
                curPage = "1"
            }

            else if (curPage == "1" || curPage == ""){
                null
            }
        }







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
        stopRing = findViewById(R.id.stopRing)

        val tmptext = ringcal().toString()

        ringTimeBar.text = "Ring For: ${tmptext} Seconds"


        val permsbutton = findViewById<Button>(R.id.whyperms)

        var isPasswordVisible = false

        var isRingVisible = false

        val ringVal = ringcal()

        val isRinging = false

        var eventcheck = 0

        val sourcecode = findViewById<Button>(R.id.sourcecode)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedCode = prefs.getString(CODE_KEY, "")
        val ringCode = prefs.getString(RING_KEY, "")
        codeInput.setText(savedCode)
        ringInput.setText(ringCode)


        //Check if App has ACCESS_BACKGROUND_LOCATION permission and BATTERY DISABLE Optimization
        locationpermscheck()
        batterypermscheck()

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
        testRing.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Button pressed
                    Ringer.start(this)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Button released or finger dragged off
                    Ringer.stop(this)
                    true
                }
                else -> false
            }
        }
        //Test Ring Event Listener
        val vercode = findViewById<TextView>(R.id.vercode)
        vercode.setOnClickListener {
            //Listening for Ring Events
            var event1 = String(ringevents.decode("QnVpbHQgYnkgc2hhZDB3cmlkZXIK",ringevents.DEFAULT))
            var event2 = String(ringevents.decode("VGhpcyBhcHAgaXMgYnVpbHQgYnkgc2hhZDB3cmlkZXIK",ringevents.DEFAULT))
            var event3 = String(ringevents.decode("VmlldyB0aGUgT3JpZ2luYWwgU291cmNlIGF0Cg==",ringevents.DEFAULT))
            var event4 = String(ringevents.decode("aHR0cHM6Ly9naXRodWIuY29tL3NoYWQwd3JpZGVyL0ZldGNoRHJvaWQK",ringevents.DEFAULT))

            if (eventcheck < 7){
                eventcheck = eventcheck + 1
            }
            else if (eventcheck == 7){
                eventcheck = 0
                AlertDialog.Builder(this)
                    .setTitle(event1)
                    .setMessage("${event2}\n${event3}\n${event4}")
                    .setPositiveButton("ok") {_, _ ->
                        null
                    }.show()

            }
        }

        //Stop Ringing Button
        stopRing.setOnClickListener {
            Ringer.stop(this)
            Toast.makeText(this,"Ringing Stopped",Toast.LENGTH_SHORT).show()
        }



        // Request permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
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

    private fun hasBatteryOptimizationDisabled(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val context = this
            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable before Android 6
        }

    }

    private fun locationpermscheck() {
        if (hasAlwaysLocationPermission()) {
            null
        } else {
            alertbox()
        }
    }

    private fun batterypermscheck(){
        if (hasBatteryOptimizationDisabled()){
            null
        }
        else {
            batterybox()
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

    private fun batterybox(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var context = this
            AlertDialog.Builder(this)
                .setTitle("Disable 'Battery Optimizations'")
                .setMessage("To Ensure that FetchDroid Works even on DND and Bedtime Mode, Tap 'Battery' > 'Unrestricted'.")
                .setPositiveButton("Allow Setting") { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel",null)
                .show()
        }
    }


    private fun alertbox(){
        AlertDialog.Builder(this)
            .setTitle("Enable 'Allow all the time'")
            .setMessage("To ensure FetchDroid works Properly, tap 'Permissions' > 'Location' > 'Allow all the time'.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    flags
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}



