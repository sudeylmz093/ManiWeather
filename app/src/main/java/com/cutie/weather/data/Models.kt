package com.cutie.weather.data

data class City(
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val lat: Double,
    val lon: Double,
    val isCurrent: Boolean = false
) {
    val subtitle: String
        get() = listOfNotNull(region, country).joinToString(", ")
}

data class HourlyItem(
    val label: String,
    val code: Int,
    val isDay: Boolean,
    val temp: Int
)

data class WeatherData(
    val city: City,
    val temp: Int,
    val high: Int,
    val low: Int,
    val humidity: Int,
    val windKmh: Int,
    val visibilityKm: Int,
    val code: Int,
    val isDay: Boolean,
    val nowMinutes: Int,
    val sunsetMinutes: Int,
    val hourly: List<HourlyItem>
)

enum class PlaceKind(val emoji: String, val label: String) {
    CAFE("☕", "Kafe"),
    RESTAURANT("🍽️", "Restoran"),
    PARK("🌳", "Park"),
    MUSEUM("🏛️", "Müze"),
    SIGHT("🌄", "Gezilecek Yer")
}

data class Place(
    val name: String,
    val kind: PlaceKind,
    val distanceM: Int,
    val lat: Double,
    val lon: Double
)

val DEFAULT_CITY = City("İstanbul", null, "Türkiye", 41.0138, 28.9497)
