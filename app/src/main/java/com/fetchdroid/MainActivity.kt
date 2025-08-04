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
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.Executor
import kotlin.math.abs
import android.util.Base64 as ringevents


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

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var donebutton: Button

    // Main UI
    private lateinit var codeInput: EditText
    private lateinit var ringInput: EditText
    private lateinit var ringTime: SeekBar
    private lateinit var ringTimeBar: TextView
    private lateinit var saveButton: Button
    private lateinit var testRing: Button
    private lateinit var stopRing: Button
    private lateinit var mainparent: ConstraintLayout

    private val PREFS_NAME = "TrackerPrefs"
    private val CODE_KEY = "codeWord"
    private val RING_KEY = "ringWord"
    private val RING_TIME_KEY = "ringTime"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

//        window.insetsController?.hide(WindowInsets.Type.navigationBars());

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
    private fun showWelcomeUI() {
        setContentView(R.layout.activity_welcome)

        //Initialize Settings Reader

        val setprefs = getSharedPreferences("TrackerPrefs",MODE_PRIVATE)


        // Get the ViewPager2 reference
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tablayer)
        val donebutton = findViewById<Button>(R.id.donebutton)

        // Set the adapter for the ViewPager2
        val adapter = ViewRunner(this)
        viewPager.adapter = adapter

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                page.scaleY = 0.85f + (1 - abs(position)) * 0.15f
            }
        }
        viewPager.setPageTransformer(transformer)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabView = LayoutInflater.from(this).inflate(R.layout.dot_view, null)
            tab.customView = tabView
        }.attach()

        fun updateTabDots(selectedPosition: Int) {
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(i)
                val dot = tab?.customView?.findViewById<ImageView>(R.id.tab_dot)
                dot?.setImageResource(if (i == selectedPosition) R.drawable.dot else R.drawable.unselected_dot)
            }
        }

        updateTabDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabDots(position)

                if (position ==9){
                    tabLayout.visibility = View.INVISIBLE
                    donebutton.visibility = View.VISIBLE
                }
                else {
                    tabLayout.visibility = View.VISIBLE
                    donebutton.visibility = View.INVISIBLE
                }
            }
        })

        donebutton.setOnClickListener {
            var chkprefs = setprefs.getString("setupok","0")

            if (chkprefs =="1"){
                showMainUI()
            }
            else {
                setprefs.edit {
                    putString("setupok","1")
                }
                showMainUI()
            }
        }


        setViewPagerHeight(viewPager, R.raw.welcome_battery_perms_dark)

    }





    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    private fun showMainUI() {

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

        setContentView(R.layout.activity_main)

        gestureToast()

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
//            val urlIntent = Intent(Intent.ACTION_VIEW,
//                "https://github.com/shad0wrider/FetchDroid".toUri())
//            startActivity(urlIntent)
            showWelcomeUI()
        }
        // permsbutton setonClick Listener
        permsbutton.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Why FetchDroid Needs these Permissions")
                .setMessage("""Permission to Access Background Location and SMS are part of FetchDroid's Core Functionality read more at the Projects Privacy Policy Page""".trimMargin())
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
            var ringtoast = Toast.makeText(this,"Press and Hold to ring , Release to Stop",Toast.LENGTH_LONG)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Button pressed
                    ringtoast.show()
                    Ringer.start(this)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Button released or finger dragged off
                    Ringer.stop(this)
                    ringtoast.cancel()
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


    private fun setViewPagerHeight(viewPager: ViewPager2,imageResId: Int){
        try{
            val inputStream = resources.openRawResource(imageResId)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val aspectratio = bitmap.width.toFloat() / bitmap.height.toFloat()

            val width = resources.displayMetrics.widthPixels
            val newheight = (width / aspectratio).toInt()

            val layoutParams = viewPager.layoutParams
            layoutParams.height = newheight
            viewPager.layoutParams = layoutParams
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun getBarHeight() : Int {


    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For Android 11 (API 30) and above
        val insets = window.decorView.rootView.rootWindowInsets
        insets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            0 
        }
    }

    private fun gestureToast(){

        Toast.makeText(this,"Navigation Bars are Hidden\nswipe up from bottom edge \nto show",Toast.LENGTH_LONG).show()
    }

}



