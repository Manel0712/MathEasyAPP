package com.example.matheasy.viewModels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.core.content.res.ResourcesCompat
import com.example.matheasy.R
import com.example.matheasy.models.ImageUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditViewModel: ViewModel() {
    private val _editLoading = MutableLiveData(false)
    public val editLoading: LiveData<Boolean> get() = _editLoading

    private val _edit = MutableLiveData<List<Alumne>>(emptyList())
    public val edit: LiveData<List<Alumne>> get() = _edit

    private val _profilePicture = MutableLiveData<ImageUpload>()
    public val profilePicture: LiveData<ImageUpload> get() = _profilePicture

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun profilePictureUpload(path: String, Bse64: String, extension: String) {
        viewModelScope.launch {
            _editLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<ImageUpload>
                resposta = Connection.service.perfilImageEdit(path, DownloadBase64Image(image = Bse64, extension = extension))
                if (resposta.isSuccessful) {
                    _profilePicture.value = resposta.body()
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
                _editLoading.value = false
            }
        }
    }

    public fun edit(Alumne: Int, Nom: String, Cognoms: String, Nom_Usuari: String, ProfilePicturePath: String, Curs: String, Experiencia: Int) {
        viewModelScope.launch {
            _editLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<Alumne>>
                resposta = Connection.service.edit(Alumne, Nom, Cognoms, Nom_Usuari, ProfilePicturePath, Curs, Experiencia)
                if (resposta.isSuccessful) {
                    _edit.value = resposta.body()
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
                _editLoading.value = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class EditViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return EditViewModel() as T
    }
}