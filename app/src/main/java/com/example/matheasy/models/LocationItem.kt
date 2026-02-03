package com.example.matheasy.models

import com.google.ar.sceneform.math.Vector3

data class LocationItem(
    val name: String,
    val lat: Double,
    val lng: Double,
    val numero: String,
    val image: String,
    var bloqueado: Boolean = true,
    var completado: Boolean = false,
    val arPosition: Vector3 = Vector3.zero()
)