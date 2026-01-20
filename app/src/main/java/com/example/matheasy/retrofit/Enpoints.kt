package com.example.matheasy.retrofit

import com.example.matheasy.models.Alumne
import com.example.matheasy.models.DownloadBase64Image
import retrofit2.Response
import retrofit2.http.*

interface Enpoints {
    @POST("api/loggin")
    suspend fun loggin(@Query("Nom_Usuari") Usuari: String, @Query("Password") Password: String): Response<List<Alumne>>

    @GET("api/perfilImage/{path}")
    suspend fun downloadBase64Image(@Query("path") path: String): Response<List<DownloadBase64Image>>
}