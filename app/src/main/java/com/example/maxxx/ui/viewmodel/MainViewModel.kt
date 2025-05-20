package com.example.maxxx.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {

    // LiveData para la posición del usuario
    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    // LiveData para un evento de búsqueda
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _searchResults = MutableLiveData<List<String>>()
    val searchResults: LiveData<List<String>> = _searchResults

    fun setSearchQuery(query: String) {
        val mockResults = listOf(
            "$query Plaza 25 de Mayo",
            "$query Mercado Central",
            "$query Universidad San Francisco",
            "$query Estadio Patria"
        )
        _searchResults.value = if (query.isNotBlank()) mockResults else emptyList()
    }

    // Función para actualizar la ubicación del usuario (simulada o con GPS)
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }


}