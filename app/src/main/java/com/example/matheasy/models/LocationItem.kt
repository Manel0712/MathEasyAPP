package com.example.matheasy.models

data class LocationItem(
    val name: String,
    val lat: Double,
    val lng: Double,
    val numero: String,
    val image: String,
    var bloqueado: Boolean = true,
    var completado: Boolean = false
)