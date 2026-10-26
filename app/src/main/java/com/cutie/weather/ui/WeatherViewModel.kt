package com.cutie.weather.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cutie.weather.data.City
import com.cutie.weather.data.DEFAULT_CITY
import com.cutie.weather.data.LocationHelper
import com.cutie.weather.data.Place
import com.cutie.weather.data.WeatherData
import com.cutie.weather.data.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: WeatherData) : UiState
    data class Error(val message: String) : UiState
}

sealed interface PlacesState {
    data object Loading : PlacesState
    data class Success(val places: List<Place>, val indoorOnly: Boolean) : PlacesState
    data object Error : PlacesState
}

private const val ERROR_TEXT = "Gökyüzüne şu an ulaşılamıyor. İnternet bağlantını kontrol et."

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    var uiState by mutableStateOf<UiState>(UiState.Loading)
        private set
    var places by mutableStateOf<PlacesState>(PlacesState.Loading)
        private set
    var scene by mutableStateOf(Scene.SUNNY)
        private set
    var night by mutableStateOf(false)
        private set
    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<City>>(emptyList())
        private set
    var searching by mutableStateOf(false)
        private set

    /** Ilk acilista konum izni/yuklemesi sadece bir kez baslasin diye. */
    var started = false

    private var retryAction: () -> Unit = { loadDefault() }
    private var lastCity: City? = null
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var placesJob: Job? = null

    fun retry() = retryAction()

    fun retryPlaces() {
        lastCity?.let { loadNearby(it) }
    }

    fun loadDefault() = loadCity(DEFAULT_CITY)

    private fun applyWeather(city: City, data: WeatherData) {
        scene = data.toScene()
        night = !data.isDay
        uiState = UiState.Success(data)
        lastCity = city
        loadNearby(city)
    }

    fun loadCity(city: City) {
        retryAction = { loadCity(city) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            uiState = UiState.Loading
            try {
                applyWeather(city, WeatherRepository.fetchWeather(city))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = UiState.Error(ERROR_TEXT)
            }
        }
    }

    fun loadMyLocation() {
        retryAction = { loadMyLocation() }
        clearSearch()
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val ctx = getApplication<Application>()
                val ll = withTimeoutOrNull(12_000) { LocationHelper.getCurrentLatLon(ctx) }
                val city = if (ll != null) {
                    LocationHelper.reverseGeocode(ctx, ll.first, ll.second)
                } else {
                    DEFAULT_CITY
                }
                applyWeather(city, WeatherRepository.fetchWeather(city))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = UiState.Error(ERROR_TEXT)
            }
        }
    }

    private fun loadNearby(city: City) {
        placesJob?.cancel()
        val indoor = scene == Scene.RAIN || scene == Scene.STORM || scene == Scene.SNOW
        placesJob = viewModelScope.launch {
            places = PlacesState.Loading
            places = try {
                PlacesState.Success(WeatherRepository.fetchNearby(city.lat, city.lon, indoor), indoor)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PlacesState.Error
            }
        }
    }

    fun onQueryChange(q: String) {
        query = q
        searchJob?.cancel()
        val trimmed = q.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            searching = false
            return
        }
        searching = true
        searchJob = viewModelScope.launch {
            delay(400)
            try {
                results = WeatherRepository.searchCities(trimmed)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                results = emptyList()
            }
            searching = false
        }
    }

    fun selectCity(city: City) {
        clearSearch()
        loadCity(city)
    }

    fun clearSearch() {
        searchJob?.cancel()
        query = ""
        results = emptyList()
        searching = false
    }
}
