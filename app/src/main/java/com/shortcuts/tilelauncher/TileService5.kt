package com.shortcuts.tilelauncher

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class TileService5 : TileService() {
    companion object {
        private const val TAG = "TileService5"
        private const val TILE_ID = 5
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "onTileAdded")
        updateTileContent()
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening")
        updateTileContent()
    }

    private fun updateTileContent() {
        val cfg = loadTileConfig(applicationContext, TILE_ID)

        qsTile?.let { tile ->
            tile.label = cfg.name.ifBlank { "Tile $TILE_ID" }
            tile.state = Tile.STATE_INACTIVE

            try {
                val icon = when (cfg.iconType) {
                    IconType.PRESET -> IconHelper.icon(applicationContext, cfg.icon, cfg.iconScale)
                    IconType.EMOJI -> IconHelper.emojiIcon(applicationContext, cfg.icon, cfg.iconScale)
                    IconType.TRANSPARENT -> IconHelper.transparentBitmap(applicationContext, cfg.icon, cfg.iconScale)
                }
                tile.icon = icon
                Log.d(TAG, "Icon set: type=${cfg.iconType}, icon=${cfg.icon}, scale=${cfg.iconScale}")
            } catch (e: Exception) {
                Log.e(TAG, "Icon failed, fallback", e)
                tile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_tile_default)
            }

            tile.updateTile()
        } ?: Log.w(TAG, "qsTile is null")
    }

    override fun onClick() {
        super.onClick()
        val cfg = loadTileConfig(applicationContext, TILE_ID)
        if (cfg.url.isBlank()) {
            Log.w(TAG, "URL empty")
            return
        }

        try {
            val intent = Intent("android.intent.action.MAIN").apply {
                setClassName("com.chimbori.hermitcrab", "com.chimbori.hermitcrab.BrowserActivity")
                data = Uri.parse(cfg.url)
                putExtra("key", cfg.key)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pi = PendingIntent.getActivity(
                applicationContext, TILE_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pi)
            Log.d(TAG, "Launched Hermit: ${cfg.url}")
        } catch (e: Exception) {
            Log.e(TAG, "Launch failed, fallback browser", e)
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(cfg.url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            applicationContext.startActivity(fallback)
        }
    }
}