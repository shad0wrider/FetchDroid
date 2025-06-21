package com.fetchdroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your main service or activity here if needed
            Toast.makeText(context, "FetchDroid Started", Toast.LENGTH_SHORT).show()

            // Example: start a foreground service if you want background tasks
            // context.startForegroundService(Intent(context, LocationService::class.java))
        }
    }
}
