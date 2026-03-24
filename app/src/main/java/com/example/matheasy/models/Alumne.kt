package com.example.matheasy.models

import java.io.Serializable

data class Resposta(
    var Resposta: AlumneResposta
) : Serializable

data class AlumneResposta(
    var Alumne: Alumne,
    var token: String? = null
) : Serializable

data class Alumne (
    var id: Int,
    var Nom: String,
    var Cognoms: String,
    var Password: String,
    var ProfilePicturePath: String,
    var Nom_Usuari: String,
    var Curs: String,
    var Nivell: Int,
    var experiencia: Experiencia,
) : Serializable