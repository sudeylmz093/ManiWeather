package com.cutie.weather.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

data class Song(val title: String)

/**
 * Hava durumuna gore Manifest sarkilari. Istedigin sarkiyi istedigin havaya tasiyabilirsin.
 * Ilk sarki varsayilan, yanindaki "sonraki" butonu ile digerine gecilir.
 */
object Songs {
    private val snap = Song("Snap")
    private val dahaIyi = Song("Daha İyi")
    private val tozPembe = Song("Toz Pembe")
    private val zamansizdik = Song("Zamansızdık")
    private val ruya = Song("Rüya")
    private val hileli = Song("Hileli")

    fun forScene(scene: Scene, night: Boolean): List<Song> {
        if (night) return listOf(ruya, zamansizdik)
        return when (scene) {
            Scene.SUNNY -> listOf(snap, dahaIyi)
            Scene.FEW_CLOUDS -> listOf(dahaIyi, snap)
            Scene.PARTLY_CLOUDY -> listOf(tozPembe, hileli)
            Scene.CLOUDY -> listOf(zamansizdik, ruya)
            Scene.HAZE -> listOf(ruya, zamansizdik)
            Scene.FOG -> listOf(zamansizdik, ruya)
            Scene.RAIN -> listOf(ruya, zamansizdik)
            Scene.STORM -> listOf(hileli, snap)
            Scene.SNOW -> listOf(tozPembe, ruya)
        }
    }
}

/** Sarkiyi YouTube'da (yuklu ise YouTube uygulamasinda) acar. */
fun openSong(context: Context, song: Song) {
    try {
        val q = URLEncoder.encode("Manifest ${song.title}", "UTF-8")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$q"))
        context.startActivity(intent)
    } catch (e: Exception) {
        // acilamazsa sessizce gec
    }
}
