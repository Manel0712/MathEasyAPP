package com.example.matheasy.retrofit

import com.example.matheasy.models.Alumne
import com.example.matheasy.models.AlumneTasca
import com.example.matheasy.models.Resposta
import com.example.matheasy.models.DownloadBase64Image
import com.example.matheasy.models.Experiencia
import com.example.matheasy.models.ImageUpload
import com.example.matheasy.models.Informe
import com.example.matheasy.models.Tema
import retrofit2.Response
import retrofit2.http.*

interface Enpoints {
    @POST("api/loggin")
    suspend fun loggin(@Query("Nom_Usuari") Usuari: String, @Query("Password") Password: String): Response<Resposta>

    @POST("api/tokenLoggin")
    suspend fun tokenLoggin(@Query("token") Token: String): Response<List<Alumne>>

    @POST("api/perfilImage")
    suspend fun perfilImageUpload(@Body body: DownloadBase64Image): Response<ImageUpload>

    @POST("api/perfilImageEdit")
    suspend fun perfilImageEdit(@Query("path") path: String, @Body body: DownloadBase64Image): Response<ImageUpload>

    @POST("api/alumnes")
    suspend fun register(@Query("Nom") Nom: String, @Query("Cognoms") Cognoms: String, @Query("Nom_Usuari") Usuari: String, @Query("Email") Email: String, @Query("Password") Password: String, @Query("ProfilePicturePath") ProfilePicturePath: String, @Query("Curs") Curs: String, @Query("Experiencia") Experiencia: Int): Response<List<Alumne>>

    @PUT("api/alumnes/{alumne}")
    suspend fun edit(@Path("alumne") alumne: Int, @Query("Nom") Nom: String, @Query("Cognoms") Cognoms: String, @Query("Nom_Usuari") Usuari: String, @Query("Email") Email: String, @Query("ProfilePicturePath") ProfilePicturePath: String, @Query("Curs") Curs: String, @Query("Experiencia") Experiencia: Int): Response<List<Alumne>>

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

    @GET("api/tasquesAlumne/{alumne}")
    suspend fun tasquesAlumne(@Path("alumne") alumne: Int): Response<List<Tema>>

    @PUT("api/respostesOperacions/{alumneTasca}")
    suspend fun afegirRespostesOperacions(@Path("alumneTasca") alumneTasca: Int, @Query("Email") Email: String, @Query("Estat_tramesa") Estat_tramesa: String, @Query("Resultat1") Resultat1: Int, @Query("Resultat2") Resultat2: Int, @Query("Resultat3") Resultat3: Int, @Query("Resultat4") Resultat4: Int, @Query("Resultat5") Resultat5: Int, @Query("Resultat6") Resultat6: Int, @Query("Resultat7") Resultat7: Int, @Query("Resultat8") Resultat8: Int, @Query("Resultat9") Resultat9: Int, @Query("Resultat10") Resultat10: Int): Response<List<AlumneTasca>>
}