package com.example.matheasy.models

import java.io.Serializable

data class Tasca (
    val id: Int,
    val Nom: String,
    val Data_obertura: String,
    val Data_tancament: String,
    var expanded: Boolean = false,
    val operacions: List<operacions>,
    val tema: Int,
    val pivot: pivot,
) : Serializable

data class Tema (
    val id: Int,
    val Nom: String,
    val tasques: List<Tasca>,
) : Serializable

data class pivot (
    val id: Int,
    val Alumne: Int,
    val Tasca: Int,
    val Qualificacio: Int?,
    val Estat_tramesa: String,
) : Serializable

data class operacions (
    val id: Int,
    val Operacio: String,
) : Serializable