package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.data.local.FakeFileRepo
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.isKeyguardSecure

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_REBOOT
        ) {
            MyLog.d("BootReceiver", "Device rebooted. Attempting to start accessibility service.")
            val serviceIntent = Intent(context, MyAccessibilityService::class.java).apply {
                action = MyAccessibilityService.ACTION_START_FOREGROUND
            }
            context.startForegroundService(serviceIntent)
        }

        if (!context.isKeyguardSecure()) {
            MyLog.fileRepo = FakeFileRepo
        } else {
            MyLog.i(TAG, "Device :${intent.action}")
        }
        if (context.hasAccessibilityPermission()) {
            context.startAccessibilityService()
        }

    }


    companion object {
        private const val TAG = "BootReceiver"
    }
}