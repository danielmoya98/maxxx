package com.example.maxxx.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maxxx.ui.model.Place
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {

    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _searchResults = MutableLiveData<List<Place>>()
    val searchResults: LiveData<List<Place>> = _searchResults

    fun setSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val mockResults = listOf(
            Place(
                name = "$query Plaza 25 de Mayo",
                address = "Centro, Sucre",
                location = LatLng(-19.0414, -65.2595)
            ),
            Place(
                name = "$query Mercado Central",
                address = "Calle Ravelo, Sucre",
                location = LatLng(-19.0420, -65.2590)
            ),
            Place(
                name = "$query Universidad San Francisco",
                address = "Av. Germán Mendoza, Sucre",
                location = LatLng(-19.0482, -65.2620)
            ),
            Place(
                name = "$query Estadio Patria",
                address = "Zona El Tejar, Sucre",
                location = LatLng(-19.0340, -65.2620)
            ),
            Place(
                name = "$query Hospital Central",
                address = "Av. Bolívar, Sucre",
                location = LatLng(-19.035, -65.264)
            )
        )

        _searchResults.value = mockResults
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }
}
