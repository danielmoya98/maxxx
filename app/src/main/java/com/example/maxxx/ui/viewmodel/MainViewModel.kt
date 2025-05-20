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
            Place("Plaza 25 de Mayo", "Centro, Sucre", LatLng(-19.0414, -65.2595)),
            Place("Mercado Central", "Calle Ravelo, Sucre", LatLng(-19.0420, -65.2590)),
            Place("Universidad San Francisco", "Av. Germán Mendoza, Sucre", LatLng(-19.0482, -65.2620)),
            Place("Estadio Patria", "Zona El Tejar, Sucre", LatLng(-19.0340, -65.2620)),
            Place("Hospital Central", "Av. Bolívar, Sucre", LatLng(-19.035, -65.264))
        )

        val filteredResults = mockResults.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
        }

        _searchResults.value = filteredResults
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }
}
