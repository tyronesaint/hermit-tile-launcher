package com.shortcuts.tilelauncher

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ConfigBackupManager {
    private const val TAG = "ConfigBackup"
    private const val VERSION = 2

    fun exportConfig(context: Context, uri: Uri): Boolean {
        return try {
            val tiles = (1..6).map { loadTileConfig(context, it) }
            val json = JSONObject().apply {
                put("version", VERSION)
                put("app_package", context.packageName)
                put("export_time", System.currentTimeMillis())
                put("tiles", JSONArray().apply {
                    tiles.forEachIndexed { index, cfg ->
                        put(JSONObject().apply {
                            put("tile_id", index + 1)
                            put("name", cfg.name)
                            put("url", cfg.url)
                            put("key", cfg.key)
                            put("icon", cfg.icon)
                            put("icon_type", cfg.iconType.name)
                            put("icon_scale", cfg.iconScale.name)
                        })
                    }
                })
            }

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                OutputStreamWriter(stream).use { writer ->
                    writer.write(json.toString(2))
                }
            }
            Log.i(TAG, "Export success to $uri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            false
        }
    }

    fun importConfig(context: Context, uri: Uri): Boolean {
        return try {
            val jsonStr = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: return false

            val json = JSONObject(jsonStr)
            val tilesArray = json.getJSONArray("tiles")

            for (i in 0 until tilesArray.length()) {
                val obj = tilesArray.getJSONObject(i)
                val tileId = obj.optInt("tile_id", i + 1)

                // 兼容老版本：如果没有 icon_type 字段，默认使用 PRESET
                val iconTypeStr = obj.optString("icon_type", "PRESET")
                val iconType = try {
                    IconType.valueOf(iconTypeStr)
                } catch (e: Exception) {
                    IconType.PRESET
                }

                // 兼容老版本：如果没有 icon_scale 字段，默认使用 MEDIUM
                val iconScaleStr = obj.optString("icon_scale", "MEDIUM")
                val iconScale = try {
                    IconHelper.Scale.valueOf(iconScaleStr)
                } catch (e: Exception) {
                    IconHelper.Scale.MEDIUM
                }

                saveTileConfig(
                    context,
                    tileId,
                    obj.optString("name", "Tile $tileId"),
                    obj.optString("url", "https://m.bing.com"),
                    obj.optString("key", ""),
                    obj.optString("icon", "search"),
                    iconType,
                    iconScale
                )
            }

            Log.i(TAG, "Import success: ${tilesArray.length()} tiles")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            false
        }
    }

    fun generateBackupFileName(): String {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "hermit_tiles_backup_$time.json"
    }
}
