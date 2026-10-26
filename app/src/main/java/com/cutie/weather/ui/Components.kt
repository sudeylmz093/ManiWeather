package com.cutie.weather.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cutie.weather.data.HourlyItem
import com.cutie.weather.data.Place
import com.cutie.weather.data.WeatherData
import java.util.Locale

@Composable
fun ThemedText(
    text: String,
    theme: WeatherTheme,
    size: TextUnit,
    weight: FontWeight = FontWeight.Bold,
    color: Color = theme.textPrimary,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            shadow = if (theme.textShadow) Shadow(Color(0x66000000), Offset(0f, 3f), 8f) else null
        )
    )
}

@Composable
fun GlassCard(
    theme: WeatherTheme,
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(theme.cardBg)
            .border(1.2.dp, theme.cardBorder, shape)
            .padding(padding),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, theme: WeatherTheme) {
    ThemedText(text, theme, 22.sp, FontWeight.ExtraBold, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
}

/** Nem / ruzgar / gorus mesafesi ve saatlik tahmin tek kartta. */
@Composable
fun InfoCard(w: WeatherData, theme: WeatherTheme) {
    GlassCard(theme, Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MiniStat("💧", "${w.humidity}%", "Nem", theme)
            MiniStat("💨", "${w.windKmh} km/sa", "Rüzgar", theme)
            MiniStat("👀", "${w.visibilityKm} km", "Görüş", theme)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            w.hourly.forEachIndexed { index, item -> HourChip(item, index == 0, theme) }
        }
    }
}

@Composable
private fun MiniStat(icon: String, value: String, label: String, theme: WeatherTheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        ThemedText(value, theme, 14.sp, FontWeight.ExtraBold, color = theme.cardText)
        ThemedText(label, theme, 11.sp, FontWeight.Medium, color = theme.cardText.copy(alpha = 0.7f))
    }
}

@Composable
private fun HourChip(item: HourlyItem, selected: Boolean, theme: WeatherTheme) {
    val bg = if (selected) theme.accent.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.16f)
    Column(
        Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThemedText(item.label, theme, 12.sp, FontWeight.SemiBold, color = theme.cardText)
        Text(WeatherCodes.emoji(item.code, item.isDay), fontSize = 20.sp)
        ThemedText("${item.temp}°", theme, 14.sp, FontWeight.ExtraBold, color = theme.cardText)
    }
}

/** Havaya uygun Manifest sarkisi. Play butonu YouTube'da acar, ok butonu siradaki sarkiya gecer. */
@Composable
fun SongCard(songs: List<Song>, theme: WeatherTheme) {
    val context = LocalContext.current
    var index by remember(songs) { mutableStateOf(0) }
    val song = songs[index % songs.size]

    GlassCard(theme, Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.accent.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) { Text("🎧", fontSize = 28.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                ThemedText(
                    "Manifest · bugünün şarkısı", theme, 12.sp, FontWeight.Medium,
                    color = theme.cardText.copy(alpha = 0.75f), maxLines = 1
                )
                ThemedText(song.title, theme, 20.sp, color = theme.cardText, maxLines = 1)
                ThemedText(
                    "Dinlemek için ▶ butonuna dokun", theme, 11.sp, FontWeight.Normal,
                    color = theme.cardText.copy(alpha = 0.6f), maxLines = 1
                )
            }
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { index += 1 },
                contentAlignment = Alignment.Center
            ) { MediaIcon(MediaKind.NEXT, theme.cardText, Modifier.size(20.dp)) }
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(theme.accent)
                    .clickable { openSong(context, song) },
                contentAlignment = Alignment.Center
            ) { MediaIcon(MediaKind.PLAY, Color(0xFF3A2150), Modifier.size(22.dp)) }
        }
    }
}

/** Havaya gore giyinmis karakter. */
@Composable
fun OutfitCard(member: Member, outfit: Outfit, temp: Int, theme: WeatherTheme) {
    GlassCard(theme, Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CuteCharacter(member, outfit, Modifier.size(width = 120.dp, height = 150.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                ThemedText(
                    "Bugünün stili · $temp°", theme, 12.sp, FontWeight.Medium,
                    color = theme.cardText.copy(alpha = 0.75f)
                )
                ThemedText("${member.name} bugün böyle giyindi", theme, 18.sp, color = theme.cardText)
                Spacer(Modifier.height(6.dp))
                ThemedText(outfit.advice, theme, 14.sp, FontWeight.Medium, color = theme.cardText)
            }
        }
    }
}

/** Yakindaki yerler: 2 sutunlu kutucuklar. */
@Composable
fun PlacesSection(
    state: PlacesState,
    theme: WeatherTheme,
    onRetry: () -> Unit
) {
    when (state) {
        PlacesState.Loading -> GlassCard(theme, Modifier.fillMaxWidth()) {
            ThemedText("Yakındaki yerler aranıyor... 🔎", theme, 16.sp, FontWeight.Medium, color = theme.cardText)
        }
        PlacesState.Error -> GlassCard(theme, Modifier.fillMaxWidth()) {
            ThemedText(
                "Yakındaki yerler şu an yüklenemedi 🥺", theme, 16.sp, FontWeight.Medium,
                color = theme.cardText
            )
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.accent)
                    .clickable { onRetry() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Tekrar dene", color = Color(0xFF3A2150), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        is PlacesState.Success -> {
            ThemedText(
                if (state.indoorOnly) "Bu havada kapalı mekanlar öneriliyor ☔" else "Hava güzel, dışarı çıkmaya ne dersin? 🌤️",
                theme, 14.sp, FontWeight.Medium, color = theme.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
            if (state.places.isEmpty()) {
                GlassCard(theme, Modifier.fillMaxWidth()) {
                    ThemedText("Yakınında kayıtlı bir yer bulamadım 🥺", theme, 16.sp, FontWeight.Medium, color = theme.cardText)
                }
            } else {
                val context = LocalContext.current
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.places.chunked(2).forEach { rowItems ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { place ->
                                PlaceCard(
                                    place, theme,
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { openPlace(context, place) }
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceCard(place: Place, theme: WeatherTheme, modifier: Modifier) {
    GlassCard(theme, modifier, padding = 14.dp) {
        Text(place.kind.emoji, fontSize = 28.sp)
        Spacer(Modifier.height(6.dp))
        ThemedText(place.name, theme, 15.sp, color = theme.cardText, maxLines = 2)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(6.dp))
        ThemedText(
            "${place.kind.label} · ${formatDistance(place.distanceM)}",
            theme, 12.sp, FontWeight.Medium,
            color = theme.cardText.copy(alpha = 0.7f), maxLines = 1
        )
    }
}

private fun formatDistance(m: Int): String =
    if (m < 1000) "$m m" else String.format(Locale.forLanguageTag("tr-TR"), "%.1f km", m / 1000.0)

/** Yeri harita uygulamasinda acar. */
fun openPlace(context: Context, place: Place) {
    try {
        val uri = Uri.parse("geo:${place.lat},${place.lon}?q=${place.lat},${place.lon}(${Uri.encode(place.name)})")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        try {
            val web = Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lon}")
            context.startActivity(Intent(Intent.ACTION_VIEW, web))
        } catch (e2: Exception) {
            // acilamazsa sessizce gec
        }
    }
}

enum class MediaKind { PLAY, NEXT }

@Composable
fun MediaIcon(kind: MediaKind, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (kind) {
            MediaKind.PLAY -> {
                val p = Path().apply {
                    moveTo(w * 0.25f, h * 0.10f)
                    lineTo(w * 0.90f, h * 0.50f)
                    lineTo(w * 0.25f, h * 0.90f)
                    close()
                }
                drawPath(p, color)
            }
            MediaKind.NEXT -> {
                val p = Path().apply {
                    moveTo(w * 0.10f, h * 0.15f)
                    lineTo(w * 0.70f, h * 0.50f)
                    lineTo(w * 0.10f, h * 0.85f)
                    close()
                }
                drawPath(p, color)
                drawRoundRect(color, Offset(w * 0.76f, h * 0.15f), Size(w * 0.14f, h * 0.70f), CornerRadius(3f))
            }
        }
    }
}
