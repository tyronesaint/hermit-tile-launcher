package com.shortcuts.tilelauncher

import android.content.Context
import android.content.SharedPreferences

enum class IconType { PRESET, EMOJI, TRANSPARENT }

data class TileConfig(
    val name: String = "",
    val url: String = "",
    val key: String = "",
    val icon: String = "search",
    val iconType: IconType = IconType.PRESET,
    val iconScale: IconHelper.Scale = IconHelper.Scale.MEDIUM
)

fun loadTileConfig(context: Context, index: Int): TileConfig {
    val prefs = context.getSharedPreferences("hermit_tiles", Context.MODE_PRIVATE)
    val name = prefs.getString("name_$index", "Tile $index") ?: "Tile $index"
    val url = prefs.getString("url_$index", "https://m.bing.com") ?: "https://m.bing.com"
    val key = prefs.getString("key_$index", "") ?: ""
    val icon = prefs.getString("icon_$index", "search") ?: "search"
    val iconTypeStr = prefs.getString("iconType_$index", "PRESET") ?: "PRESET"
    val iconScaleStr = prefs.getString("iconScale_$index", "MEDIUM") ?: "MEDIUM"

    val iconType = try {
        IconType.valueOf(iconTypeStr)
    } catch (e: Exception) {
        IconType.PRESET
    }

    val iconScale = try {
        IconHelper.Scale.valueOf(iconScaleStr)
    } catch (e: Exception) {
        IconHelper.Scale.MEDIUM
    }

    return TileConfig(name, url, key, icon, iconType, iconScale)
}

fun saveTileConfig(context: Context, index: Int, name: String, url: String, key: String, icon: String, iconType: IconType = IconType.PRESET, iconScale: IconHelper.Scale = IconHelper.Scale.MEDIUM) {
    val prefs = context.getSharedPreferences("hermit_tiles", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("name_$index", name)
        .putString("url_$index", url)
        .putString("key_$index", key)
        .putString("icon_$index", icon)
        .putString("iconType_$index", iconType.name)
        .putString("iconScale_$index", iconScale.name)
        .apply()
}

fun generateHermitKey(url: String): String {
    val cleanUrl = url.replace("https://", "").replace("http://", "").trimEnd('/')
    val timestamp = System.currentTimeMillis().toString().takeLast(8)
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    val randomSuffix = (1..8).map { chars.random() }.joinToString("")
    return "$cleanUrl-$randomSuffix"
}
