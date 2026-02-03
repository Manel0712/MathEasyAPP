package com.example.matheasy.retrofit

import android.R
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.DownloadBase64Image
import com.example.matheasy.models.Experiencia
import com.example.matheasy.models.ImageUpload
import com.example.matheasy.models.Informe
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

    @GET("api/alumnes/experiencia/{experiencia}")
    suspend fun experiencia(@Path("experiencia") experiencia: Int): Response<List<Experiencia>>

    @PUT("api/alumnes/experiencia/{experiencia}")
    suspend fun experienciaUpdate(@Path("experiencia") experiencia: Int, @Query("Nivell") Nivell: Int, @Query("Total_xp") Total_xp: Int, @Query("Medalles") Medalles: Int): Response<List<Experiencia>>

    @POST("api/informes")
    suspend fun informeCreated(@Query("Tipus_partida") Tipus_partida: String, @Query("Respostes_correctes") Respostes_correctes: Int, @Query("Respostes_incorrectes") Respostes_incorrectes: Int, @Query("Experiencia") Experiencia: Int, @Query("alumne_id") alumne_id: Int): Response<List<Informe>>

    @GET("api/informesAlumne/{alumne}")
    suspend fun informesAlumne(@Path("alumne") alumne: Int): Response<List<Informe>>

    @PUT("api/alumnes/{alumne}")
    suspend fun editLevel(@Path("alumne") alumne: Int, @Query("Nivell") Nivell: Int): Response<List<Alumne>>
}