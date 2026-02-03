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
import com.example.matheasy.models.Informe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class InformesViewModel: ViewModel() {
    private val _informesLoading = MutableLiveData(false)
    public val informesLoading: LiveData<Boolean> get() = _informesLoading

    private val _informes = MutableLiveData<List<Informe>>(emptyList())
    public val informes: LiveData<List<Informe>> get() = _informes

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun informes(alumne: Int) {
        viewModelScope.launch {
            _informesLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<Informe>>
                resposta = Connection.service.informesAlumne(alumne)
                if (resposta.isSuccessful) {
                    _informes.value = resposta.body()
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
                _informesLoading.value = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class InformesViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return InformesViewModel() as T
    }
}