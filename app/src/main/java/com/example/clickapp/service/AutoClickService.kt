package com.example.clickapp.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoClickService : AccessibilityService() {
    private var flag = false
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (flag) return
        flag = true

        scope.launch {
            try {
                // Step 1: Open Instagram
                openInstagram()

                // Step 2: Go to Notifications
                delay(3000) // Wait for Instagram to load
                goToNotifications(event)


                // Step 3: Open App Info
                delay(7000) // Wait for notification actions
                openAppInfo()

                // Step 4: Click Force Stop
                delay(2000) // Wait for App Info to load
                clickForceStop(event)


                // Step 5: Click OK
                delay(2000) // Wait for confirmation dialog
                clickOK(event)

                Log.d("AutoClickService", "All steps completed successfully!")
            } finally {
                flag = false
                disableSelf() // Disable the service
            }
        }
    }

    private fun openInstagram() {
        val intentToInstagram = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"))
        intentToInstagram.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentToInstagram)
        Log.e("AutoClickService", "Instagram opened")
    }

    private suspend fun goToNotifications(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        withContext(Dispatchers.IO) {
            return@withContext findAndClickButtonByText(root, "Notifications")
        }
    }

    private fun openAppInfo() {
        val intentToAppInfo = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intentToAppInfo.data = Uri.parse("package:com.instagram.android")
        intentToAppInfo.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentToAppInfo)
        Log.e("AutoClickService", "App Info opened")
    }

    private suspend fun clickForceStop(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        withContext(Dispatchers.IO) {
            return@withContext findAndClickButtonByText(root, "Force stop")
        }
    }

    private suspend fun clickOK(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        withContext(Dispatchers.IO) {
            return@withContext findAndClickButtonByText(root, "OK")
        }
    }

    override fun onInterrupt() {}


    private fun findAndClickButtonByText(rootNode: AccessibilityNodeInfo, text: String): Boolean {
        for (i in 0 until rootNode.childCount) {
            val childNode = rootNode.getChild(i) ?: continue
            if (
                (childNode.text != null && childNode.text.toString().contains(text, ignoreCase = true)) ||
                (childNode.contentDescription != null && childNode.contentDescription.toString().contains(text, ignoreCase = true))
            ) {
                Log.e("findAndClickButtonByText", "Found button: $text")
                childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            if (findAndClickButtonByText(childNode, text)) return true
        }
        return false
    }

}
