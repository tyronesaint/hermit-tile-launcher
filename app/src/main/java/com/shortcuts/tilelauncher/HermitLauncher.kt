package com.shortcuts.tilelauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object HermitLauncher {
    private const val TAG = "HermitLauncher"

    fun launch(context: Context, url: String, key: String) {
        Log.d(TAG, "Launching Hermit: url=$url, key=$key")
        try {
            val intent = Intent("android.intent.action.MAIN").apply {
                setClassName("com.chimbori.hermitcrab", "com.chimbori.hermitcrab.BrowserActivity")
                data = Uri.parse(url)
                putExtra("key", key)
            }
            context.startActivity(intent)
            Log.d(TAG, "Hermit launched")
        } catch (e: Exception) {
            Log.e(TAG, "Failed: ${e.message}", e)
            // 降级：尝试用浏览器打开
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
