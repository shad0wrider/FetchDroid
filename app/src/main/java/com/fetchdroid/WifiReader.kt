package com.fetchdroid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.annotation.RequiresPermission
import java.util.concurrent.Executor

class WifiReader(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun getTopFiveSSIDs(): List<String> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val scanResults = wifiManager.scanResults

        return scanResults
            .filter { it.wifiSsid != null && it.wifiSsid!!.toString().isNotBlank() }
            .distinctBy { it.wifiSsid!!.toString() }
            .sortedByDescending { it.level }
            .map { it.wifiSsid!!.toString() }
            .take(5)
    }
}