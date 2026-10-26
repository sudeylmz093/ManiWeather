package com.cutie.weather.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    /** Anlik konumu (enlem, boylam) dondurur. Alinamazsa null. Izin onceden verilmis olmali. */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLatLon(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        if (cont.isActive) cont.resume(Pair(loc.latitude, loc.longitude))
                    } else {
                        client.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) {
                                    cont.resume(if (last != null) Pair(last.latitude, last.longitude) else null)
                                }
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

    /** Koordinattan sehir adini bulur (Android Geocoder). */
    suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): City =
        withContext(Dispatchers.IO) {
            var name: String? = null
            var region: String? = null
            var country: String? = null
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.forLanguageTag("tr-TR"))
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocation(lat, lon, 1)
                    val a = list?.firstOrNull()
                    if (a != null) {
                        name = a.locality ?: a.subAdminArea ?: a.adminArea
                        region = a.adminArea
                        country = a.countryName
                    }
                }
            } catch (e: Exception) {
                // isimsiz devam et
            }
            val finalName = name ?: "Konumum"
            City(
                name = finalName,
                region = if (region == finalName) null else region,
                country = country,
                lat = lat,
                lon = lon,
                isCurrent = true
            )
        }
}
