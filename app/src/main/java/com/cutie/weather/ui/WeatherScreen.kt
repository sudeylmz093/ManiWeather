package com.cutie.weather.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.cutie.weather.data.City
import com.cutie.weather.data.WeatherData

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@Composable
fun WeatherApp(vm: WeatherViewModel) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val lightText = vm.night || vm.scene.darkBg
    val theme = Themes.of(lightText)
    val scroll = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) vm.loadMyLocation() else vm.loadDefault()
    }

    fun requestLocation() {
        if (hasLocationPermission(context)) {
            vm.loadMyLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!vm.started) {
            vm.started = true
            requestLocation()
        }
    }

    // Yeni sehir/yenileme olunca sayfa basa donsun
    LaunchedEffect(vm.uiState) { scroll.scrollTo(0) }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !lightText
            controller.isAppearanceLightNavigationBars = !lightText
        }
    }

    MaterialTheme {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF2A2A4A))
        ) {
            val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            // ilk sayfa: ekran - cubuklar - arama cubugu alani
            val pageHeight = maxHeight - topInset - bottomInset - 76.dp

            AnimatedBackground(vm.scene, vm.night)

            // Kaydirdikca icerigin okunmasi icin hafif ortu
            val scrimAlpha = (scroll.value / 1400f).coerceIn(0f, 0.45f)
            Box(
                Modifier
                    .fillMaxSize()
                    .background((if (lightText) Color.Black else Color.White).copy(alpha = scrimAlpha))
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                SearchBar(
                    query = vm.query,
                    theme = theme,
                    focus = focus,
                    onQueryChange = vm::onQueryChange,
                    onMyLocation = {
                        focus.clearFocus()
                        vm.clearSearch()
                        requestLocation()
                    }
                )
                Spacer(Modifier.height(12.dp))

                if (vm.query.isNotBlank()) {
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SearchResults(
                            query = vm.query,
                            results = vm.results,
                            searching = vm.searching,
                            theme = theme,
                            onPick = { city ->
                                focus.clearFocus()
                                vm.selectCity(city)
                            }
                        )
                    }
                } else {
                    when (val state = vm.uiState) {
                        UiState.Loading -> Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { LoadingView(theme) }

                        is UiState.Error -> Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { ErrorView(state.message, theme) { vm.retry() } }

                        is UiState.Success -> Column(
                            Modifier
                                .weight(1f)
                                .verticalScroll(scroll)
                        ) {
                            HeroPage(state.data, vm.scene, theme, Modifier.height(pageHeight))
                            Spacer(Modifier.height(16.dp))

                            SectionTitle("🎧 Bugünün Manifest Şarkısı", theme)
                            SongCard(Songs.forScene(vm.scene, vm.night), theme)
                            Spacer(Modifier.height(16.dp))

                            val member = memberFor(state.data, vm.scene, vm.night)
                            OutfitCard(member, outfitFor(state.data, vm.scene), state.data.temp, theme)
                            Spacer(Modifier.height(28.dp))

                            SectionTitle("📍 Yakınında", theme)
                            PlacesSection(vm.places, theme) { vm.retryPlaces() }
                            Spacer(Modifier.height(16.dp))

                            ThemedText(
                                "Hava verisi: Open-Meteo.com · Yerler: © OpenStreetMap katkıda bulunanlar",
                                theme, 11.sp, FontWeight.Normal,
                                color = theme.textSecondary,
                                align = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/** 1. sayfa: sehir, sicaklik, saatlik tahmin. Karakterler arka plan gorselinde gorunur. */
@Composable
private fun HeroPage(w: WeatherData, scene: Scene, theme: WeatherTheme, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "hint")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(0.62f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThemedText(
                    w.city.name, theme, 30.sp, FontWeight.ExtraBold,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (w.city.isCurrent) Text(" 📍", fontSize = 20.sp)
            }
            ThemedText(
                if (scene == Scene.HAZE) "Puslu" else WeatherCodes.description(w.code, w.isDay),
                theme, 18.sp, FontWeight.Medium, color = theme.textSecondary
            )
            ThemedText("${w.temp}°", theme, 84.sp, FontWeight.ExtraBold)
            ThemedText(
                "En yüksek: ${w.high}°   En düşük: ${w.low}°",
                theme, 14.sp, FontWeight.SemiBold, color = theme.textSecondary
            )
        }

        Spacer(Modifier.weight(1f))

        InfoCard(w, theme)
        Spacer(Modifier.height(10.dp))
        ThemedText(
            "Şarkı, kıyafet ve yakın yerler için kaydır ↓",
            theme, 12.sp, FontWeight.Medium,
            color = theme.textSecondary,
            align = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = bob.dp)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    theme: WeatherTheme,
    focus: FocusManager,
    onQueryChange: (String) -> Unit,
    onMyLocation: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .background(theme.cardBg)
                .border(1.dp, theme.cardBorder, shape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Ara", tint = theme.cardText.copy(alpha = 0.8f))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Şehir ara...", color = theme.cardText.copy(alpha = 0.6f), fontSize = 16.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = theme.cardText, fontSize = 16.sp),
                    cursorBrush = SolidColor(theme.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Temizle",
                    tint = theme.cardText.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(theme.cardBg)
                .border(1.dp, theme.cardBorder, CircleShape)
                .clickable { onMyLocation() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "Konumum", tint = theme.accent)
        }
    }
}

@Composable
private fun SearchResults(
    query: String,
    results: List<City>,
    searching: Boolean,
    theme: WeatherTheme,
    onPick: (City) -> Unit
) {
    GlassCard(theme, Modifier.fillMaxWidth(), padding = 18.dp) {
        when {
            query.trim().length < 2 ->
                ThemedText("En az 2 harf yaz...", theme, 16.sp, FontWeight.Medium, color = theme.cardText)
            searching ->
                ThemedText("Haritada aranıyor... 🗺️", theme, 16.sp, FontWeight.Medium, color = theme.cardText)
            results.isEmpty() ->
                ThemedText("Şehir bulunamadı 🥺", theme, 16.sp, FontWeight.Medium, color = theme.cardText)
            else -> results.forEachIndexed { index, city ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(city) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        ThemedText(city.name, theme, 17.sp, color = theme.cardText)
                        if (city.subtitle.isNotEmpty()) {
                            ThemedText(
                                city.subtitle, theme, 13.sp, FontWeight.Medium,
                                color = theme.cardText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                if (index < results.lastIndex) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(theme.cardBorder.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView(theme: WeatherTheme) {
    val transition = rememberInfiniteTransition(label = "load")
    val s by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "scale"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🌤️", fontSize = 64.sp, modifier = Modifier.scale(s))
        Spacer(Modifier.height(12.dp))
        ThemedText("Gökyüzüne bakılıyor...", theme, 18.sp, FontWeight.SemiBold)
    }
}

@Composable
private fun ErrorView(message: String, theme: WeatherTheme, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🥺", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        ThemedText(message, theme, 16.sp, FontWeight.Medium, align = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(theme.accent)
                .clickable { onRetry() }
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text("Tekrar dene", color = Color(0xFF3A2150), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
