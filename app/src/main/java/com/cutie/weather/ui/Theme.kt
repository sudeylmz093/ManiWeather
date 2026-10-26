package com.cutie.weather.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.cutie.weather.R
import com.cutie.weather.data.WeatherData
import kotlin.math.abs

/** Her hava durumu icin senin gorselin. Gorselleri res/drawable-nodpi icinde degistirebilirsin. */
enum class Scene(@DrawableRes val image: Int, val darkBg: Boolean) {
    SUNNY(R.drawable.bg_gunesli, false),
    FEW_CLOUDS(R.drawable.bg_az_bulutlu, false),
    PARTLY_CLOUDY(R.drawable.bg_parcali_bulutlu, false),
    CLOUDY(R.drawable.bg_cok_bulutlu, false),
    HAZE(R.drawable.bg_puslu, false),
    FOG(R.drawable.bg_sisli, false),
    RAIN(R.drawable.bg_yagmurlu, false),
    STORM(R.drawable.bg_firtina, true),
    SNOW(R.drawable.bg_karli, true)
}

data class WeatherTheme(
    val lightText: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val cardText: Color,
    val accent: Color,
    val textShadow: Boolean
)

object Themes {
    private val onDark = WeatherTheme(
        lightText = true,
        textPrimary = Color.White,
        textSecondary = Color(0xFFE8EAFB),
        cardBg = Color(0x8C0F1233),
        cardBorder = Color(0x55FFFFFF),
        cardText = Color.White,
        accent = Color(0xFFFF9CC8),
        textShadow = true
    )
    private val onLight = WeatherTheme(
        lightText = false,
        textPrimary = Color(0xFF3A2A5C),
        textSecondary = Color(0xFF5E4F80),
        cardBg = Color(0xB3FFFFFF),
        cardBorder = Color(0xCCFFFFFF),
        cardText = Color(0xFF3A2A5C),
        accent = Color(0xFFFF8FB8),
        textShadow = false
    )

    fun of(lightText: Boolean): WeatherTheme = if (lightText) onDark else onLight
}

/** Hava kodundan hangi arka plan gorselinin gosterilecegini secer. */
fun WeatherData.toScene(): Scene = when {
    code in 95..99 -> Scene.STORM
    code in 71..77 || code == 85 || code == 86 -> Scene.SNOW
    code in 51..67 || code in 80..82 -> Scene.RAIN
    code == 45 || code == 48 -> Scene.FOG
    visibilityKm < 5 -> Scene.HAZE
    code == 0 -> Scene.SUNNY
    code == 1 -> Scene.FEW_CLOUDS
    code == 2 -> Scene.PARTLY_CLOUDY
    else -> Scene.CLOUDY
}

/** Hava durumuna gore hangi Manifest uyesinin gosterilecegi. Kuralları buradan degistirebilirsin. */
fun memberFor(w: WeatherData, scene: Scene, night: Boolean): Member {
    val nearSunset = !night && abs(w.nowMinutes - w.sunsetMinutes) <= 75 && w.code <= 3
    return when {
        night -> if (w.code <= 1) Members.zoktay else Members.hilal
        scene == Scene.STORM -> Members.zoktay
        scene == Scene.RAIN -> Members.mina
        scene == Scene.SNOW -> Members.lidya
        w.windKmh >= 20 && w.code <= 2 -> Members.sueda
        nearSunset -> Members.mina
        scene == Scene.SUNNY || scene == Scene.FEW_CLOUDS -> Members.esin
        else -> Members.hilal
    }
}

/** Sicaklik ve havaya gore kiyafet. */
fun outfitFor(w: WeatherData, scene: Scene): Outfit = when {
    scene == Scene.RAIN || scene == Scene.STORM -> Outfit.RAINCOAT
    scene == Scene.SNOW || w.temp <= 6 -> Outfit.WINTER
    w.temp <= 16 -> Outfit.JACKET
    w.temp >= 26 -> Outfit.SUMMER
    else -> Outfit.TEE
}

object WeatherCodes {
    fun description(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "Güneşli" else "Açık Gece"
        1 -> if (isDay) "Az Bulutlu" else "Açık Gece"
        2 -> if (isDay) "Parçalı Bulutlu" else "Bulutlu Gece"
        3 -> if (isDay) "Çok Bulutlu" else "Bulutlu Gece"
        45, 48 -> "Sisli"
        in 51..57 -> "Çiseleme"
        in 61..67 -> "Yağmurlu"
        in 71..77 -> "Karlı"
        in 80..82 -> "Sağanak Yağış"
        85, 86 -> "Kar Sağanağı"
        in 95..99 -> "Gök Gürültülü Fırtına"
        else -> "Güzel Gökyüzü"
    }

    fun emoji(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1 -> if (isDay) "🌤️" else "🌙"
        2 -> if (isDay) "⛅" else "☁️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..57 -> "🌦️"
        in 61..67, in 80..82 -> "🌧️"
        in 71..77, 85, 86 -> "❄️"
        in 95..99 -> "⛈️"
        else -> "✨"
    }
}
