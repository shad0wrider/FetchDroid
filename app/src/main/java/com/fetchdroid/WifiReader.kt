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
