package com.example.matheasy.models

import java.io.Serializable

data class Alumne (
    var id: Int,
    var Nom: String,
    var Cognoms: String,
    var Password: String,
    var ProfilePicturePath: String,
    var Nom_Usuari: String,
    var Curs: String,
    var Experiencia: Int = 0,
) : Serializable