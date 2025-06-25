package com.fetchdroid

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.SmsMessage


class SmsReceiver : BroadcastReceiver() {

    private var locationSent = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return
        val format = bundle.getString("format")

        val prefs = context!!.getSharedPreferences("TrackerPrefs", Context.MODE_PRIVATE)
        val codeWord = prefs.getString("codeWord", "") ?: return
        val ringWord = prefs.getString("ringWord", "") ?: return

        for (pdu in pdus) {
            val message = SmsMessage.createFromPdu(pdu as ByteArray, format)
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody.trim()
            val tmpsms = context.getSystemService(SmsManager::class.java)

            if (body == codeWord) {
                tmpsms.sendTextMessage(sender,null,"Fetching Location\nPlease Wait...",null,null)
                getLocationAndRespond(context, sender)
            }
        }
    }

    fun getBatteryPercentage(context: Context): Int {
        val battery = context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (scale > 0 && level >= 0) (level * 100 / scale) else -1
    }


    @SuppressLint("MissingPermission", "ServiceCast")
    private fun getLocationAndRespond(context: Context, phoneNumber: String) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var hasSent = false
        var fallbackUsed = false
        val handler = Handler(Looper.getMainLooper())

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (hasSent) return  // Prevent multiple sends

                val accuracy = location.accuracy
                val isAccurateEnough = accuracy <= 20f && location.provider == LocationManager.GPS_PROVIDER

                if (isAccurateEnough || fallbackUsed) {
                    hasSent = true
                    locationManager.removeUpdates(this)
                    handler.removeCallbacksAndMessages(null) // cancel timeout

                    val ssids = WifiReader(context).getTopFiveSSIDs()
                    val wifiText = if (ssids.isNotEmpty()) "Nearby Wi-Fi: ${ssids.joinToString(", ")}" else "No Wi-Fi nearby"
                    val lat = location.latitude
                    val lon = location.longitude
                    val reply = "Latitude: $lat\nLongitude: $lon"
                    val mapsloc = "https://maps.google.com/?q=${lat}%2C${lon}"
                    val batpercent = "Battery: ${getBatteryPercentage(context)}%"

                    val smsManager = context.getSystemService(SmsManager::class.java)
                    if (location.provider == LocationManager.GPS_PROVIDER ) {
                        smsManager.sendTextMessage(phoneNumber, null, reply, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, mapsloc, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, wifiText, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, batpercent, null, null)
                        smsManager.sendTextMessage(phoneNumber,null,"provider: GPS",null,null)
                    }
                    else{
                        smsManager.sendTextMessage(phoneNumber, null, reply, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, mapsloc, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, wifiText, null, null)
                        smsManager.sendTextMessage(phoneNumber, null, batpercent, null, null)
                        smsManager.sendTextMessage(phoneNumber,null,"provider: Network",null,null)                        
                    }
                }
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        // Try GNSS first
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener,
                Looper.getMainLooper()
            )

            // Fallback to network after 30 seconds
            handler.postDelayed({
                if (!hasSent) {
                    fallbackUsed = true
                    locationManager.removeUpdates(listener)
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0L,
                            0f,
                            listener,
                            Looper.getMainLooper()
                        )
                    }
                }
            }, 30000L)
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Direct fallback if GPS is unavailable
            fallbackUsed = true
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                listener,
                Looper.getMainLooper()
            )
        } else {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, "Location unavailable", null, null)
        }
    }
    
}
