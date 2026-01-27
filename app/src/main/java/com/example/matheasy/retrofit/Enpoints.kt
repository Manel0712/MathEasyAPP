package com.example.matheasy.retrofit

import android.R
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.DownloadBase64Image
import com.example.matheasy.models.ImageUpload
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Enpoints {
    @POST("api/loggin")
    suspend fun loggin(@Query("Nom_Usuari") Usuari: String, @Query("Password") Password: String): Response<List<Alumne>>

    @POST("api/perfilImage")
    suspend fun perfilImageUpload(@Body body: DownloadBase64Image): Response<ImageUpload>

    @POST("api/perfilImageEdit")
    suspend fun perfilImageEdit(@Query("path") path: String, @Body body: DownloadBase64Image): Response<ImageUpload>

    @POST("api/alumnes")
    suspend fun register(@Query("Nom") Nom: String, @Query("Cognoms") Cognoms: String, @Query("Nom_Usuari") Usuari: String, @Query("Password") Password: String, @Query("ProfilePicturePath") ProfilePicturePath: String, @Query("Curs") Curs: String, @Query("Experiencia") Experiencia: Int): Response<List<Alumne>>

    @PUT("api/alumnes/{alumne}")
    suspend fun edit(@Path("alumne") alumne: Int, @Query("Nom") Nom: String, @Query("Cognoms") Cognoms: String, @Query("Nom_Usuari") Usuari: String, @Query("ProfilePicturePath") ProfilePicturePath: String, @Query("Curs") Curs: String, @Query("Experiencia") Experiencia: Int): Response<List<Alumne>>
}