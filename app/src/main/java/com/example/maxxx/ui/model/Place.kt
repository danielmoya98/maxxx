package com.example.maxxx.ui.model

import com.google.android.gms.maps.model.LatLng

data class Place(
    val name: String,
    val address: String,
    val location: LatLng,
    val horario: String,
    val imagenUrl: String
)
