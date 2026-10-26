package com.cutie.weather.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Open-Meteo kullanir: ucretsiz, API anahtari gerektirmez. */
object WeatherRepository {

    private suspend fun httpGet(url: String, timeoutMs: Int = 15000): String = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 15000
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("User-Agent", "ManifestHava/1.0 (Android)")
            conn.requestMethod = "GET"
            if (conn.responseCode !in 200..299) {
                throw IOException("HTTP ${conn.responseCode}")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    suspend fun searchCities(query: String): List<City> {
        val q = URLEncoder.encode(query, "UTF-8")
        val body = httpGet(
            "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=8&language=tr&format=json"
        )
        val arr = JSONObject(body).optJSONArray("results") ?: return emptyList()
        val list = ArrayList<City>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                City(
                    name = o.getString("name"),
                    region = o.optString("admin1").ifBlank { null },
                    country = o.optString("country").ifBlank { null },
                    lat = o.getDouble("latitude"),
                    lon = o.getDouble("longitude")
                )
            )
        }
        return list
    }

    suspend fun fetchWeather(city: City): WeatherData {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${city.lat}&longitude=${city.lon}" +
            "&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m,visibility" +
            "&hourly=temperature_2m,weather_code,is_day" +
            "&daily=temperature_2m_max,temperature_2m_min,sunset" +
            "&timezone=auto&forecast_days=2"

        val root = JSONObject(httpGet(url))
        val cur = root.getJSONObject("current")
        val hourly = root.getJSONObject("hourly")
        val daily = root.getJSONObject("daily")

        val timeNow = cur.getString("time") // ornek: 2026-10-26T14:15
        val nowHour = timeNow.substring(0, 13) + ":00"

        val times = hourly.getJSONArray("time")
        val temps = hourly.getJSONArray("temperature_2m")
        val codes = hourly.getJSONArray("weather_code")
        val days = hourly.getJSONArray("is_day")

        var start = 0
        for (i in 0 until times.length()) {
            if (times.getString(i) >= nowHour) {
                start = i
                break
            }
        }
        val items = ArrayList<HourlyItem>()
        val end = min(start + 12, times.length())
        for (i in start until end) {
            val hour = times.getString(i).substring(11, 13).toInt()
            items.add(
                HourlyItem(
                    label = if (i == start) "Şimdi" else hourLabel(hour),
                    code = codes.getInt(i),
                    isDay = days.getInt(i) == 1,
                    temp = temps.getDouble(i).roundToInt()
                )
            )
        }

        return WeatherData(
            city = city,
            temp = cur.getDouble("temperature_2m").roundToInt(),
            high = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
            low = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
            humidity = cur.getDouble("relative_humidity_2m").roundToInt(),
            windKmh = cur.getDouble("wind_speed_10m").roundToInt(),
            visibilityKm = (cur.optDouble("visibility", 10000.0) / 1000.0).roundToInt(),
            code = cur.getInt("weather_code"),
            isDay = cur.getInt("is_day") == 1,
            nowMinutes = minutesOf(timeNow),
            sunsetMinutes = minutesOf(daily.getJSONArray("sunset").getString(0)),
            hourly = items
        )
    }

    private fun minutesOf(iso: String): Int =
        iso.substring(11, 13).toInt() * 60 + iso.substring(14, 16).toInt()

    private fun hourLabel(h: Int): String = h.toString().padStart(2, '0') + ":00"

    /** OpenStreetMap (Overpass) ile yakindaki yerler. Yagmur/kar/firtinada sadece kapali mekanlar. */
    suspend fun fetchNearby(lat: Double, lon: Double, indoorOnly: Boolean): List<Place> {
        val around = "(around:2500,$lat,$lon)"
        val parts = StringBuilder()
        parts.append("nwr[\"amenity\"=\"cafe\"]$around;")
        parts.append("nwr[\"amenity\"=\"restaurant\"]$around;")
        parts.append("nwr[\"tourism\"=\"museum\"]$around;")
        if (!indoorOnly) {
            parts.append("nwr[\"leisure\"=\"park\"]$around;")
            parts.append("nwr[\"tourism\"~\"attraction|viewpoint\"]$around;")
        }
        val query = "[out:json][timeout:20];($parts);out center 120;"
        val body = httpGet(
            "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8"),
            30000
        )
        val elements = JSONObject(body).optJSONArray("elements") ?: return emptyList()
        val all = ArrayList<Place>()
        val seen = HashSet<String>()
        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            val tags = el.optJSONObject("tags") ?: continue
            val name = tags.optString("name")
            if (name.isBlank()) continue
            val amenity = tags.optString("amenity")
            val tourism = tags.optString("tourism")
            val leisure = tags.optString("leisure")
            val kind = when {
                amenity == "cafe" -> PlaceKind.CAFE
                amenity == "restaurant" -> PlaceKind.RESTAURANT
                tourism == "museum" -> PlaceKind.MUSEUM
                leisure == "park" -> PlaceKind.PARK
                tourism == "attraction" || tourism == "viewpoint" -> PlaceKind.SIGHT
                else -> continue
            }
            val pLat: Double
            val pLon: Double
            if (el.has("lat")) {
                pLat = el.getDouble("lat")
                pLon = el.getDouble("lon")
            } else {
                val c = el.optJSONObject("center") ?: continue
                pLat = c.getDouble("lat")
                pLon = c.getDouble("lon")
            }
            if (!seen.add("$name|$kind")) continue
            all.add(Place(name, kind, distanceMeters(lat, lon, pLat, pLon), pLat, pLon))
        }
        return all.groupBy { it.kind }
            .values
            .flatMap { list -> list.sortedBy { it.distanceM }.take(4) }
            .sortedBy { it.distanceM }
            .take(12)
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * r * asin(sqrt(a))).roundToInt()
    }
}
