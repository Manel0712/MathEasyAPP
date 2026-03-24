package com.example.matheasy.viewModels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.launch
import androidx.lifecycle.*
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.DownloadBase64Image
import com.example.matheasy.retrofit.Connection
import java.io.IOException
import retrofit2.Response
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import okhttp3.ResponseBody
import java.io.OutputStream
import android.provider.MediaStore
import com.example.matheasy.models.AlumneTasca
import com.example.matheasy.models.Resposta
import com.example.matheasy.models.Tema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class TemesViewModel: ViewModel() {
    private val _temaLoading = MutableLiveData(false)
    public val temaLoading: LiveData<Boolean> get() = _temaLoading

    private val _tema = MutableLiveData<List<Tema>>()
    public val tema: LiveData<List<Tema>> get() = _tema

    private val _entrega = MutableLiveData<List<AlumneTasca>>()
    public val entrega: LiveData<List<AlumneTasca>> get() = _entrega

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun temes(alumne: Int) {
        viewModelScope.launch {
            _temaLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<Tema>>
                resposta = Connection.service.tasquesAlumne(alumne)
                if (resposta.isSuccessful) {
                    _tema.value = resposta.body()
                }
                else {
                    _error.value = "ERROR CODE: " + resposta.code().toString()
                }
            }
            catch (e: IOException) {
                _error.value = "Error de xarxa"
            }
            catch (e: Exception) {
                _error.value = "Error desconocido: ${e.localizedMessage}"
            }
            finally {
                _temaLoading.value = false
            }
        }
    }

    public fun entregues(Email: String, alumneTasca: Int, Estat_tramesa: String, Resultat1: Int, Resultat2: Int, Resultat3: Int, Resultat4: Int, Resultat5: Int, Resultat6: Int, Resultat7: Int, Resultat8: Int, Resultat9: Int, Resultat10: Int) {
        viewModelScope.launch {
            _temaLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<AlumneTasca>>
                resposta = Connection.service.afegirRespostesOperacions(alumneTasca, Email, Estat_tramesa, Resultat1, Resultat2, Resultat3, Resultat4, Resultat5, Resultat6, Resultat7, Resultat8, Resultat9, Resultat10)
                if (resposta.isSuccessful) {
                    _entrega.value = resposta.body()
                }
                else {
                    _error.value = "ERROR CODE: " + resposta.code().toString()
                }
            }
            catch (e: IOException) {
                _error.value = "Error de xarxa"
            }
            catch (e: Exception) {
                _error.value = "Error desconocido: ${e.localizedMessage}"
            }
            finally {
                _temaLoading.value = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TemesViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return TemesViewModel() as T
    }
}