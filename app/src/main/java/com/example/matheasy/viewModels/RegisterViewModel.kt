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

class RegisterViewModel: ViewModel() {
    private val _registerLoading = MutableLiveData(false)
    public val registerLoading: LiveData<Boolean> get() = _registerLoading

    private val _register = MutableLiveData<List<Alumne>>(emptyList())
    public val register: LiveData<List<Alumne>> get() = _register

    private val _profilePicture = MutableLiveData<ImageUpload>()
    public val profilePicture: LiveData<ImageUpload> get() = _profilePicture

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun profilePictureUpload(Bse64: String, extension: String) {
        viewModelScope.launch {
            _registerLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<ImageUpload>
                resposta = Connection.service.perfilImageUpload(DownloadBase64Image(image = Bse64, extension = extension))
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
                _registerLoading.value = false
            }
        }
    }

    public fun register(Nom: String, Cognoms: String, Nom_Usuari: String, Password: String, ProfilePicturePath: String, Curs: String, Experiencia: Int) {
        viewModelScope.launch {
            _registerLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<Alumne>>
                resposta = Connection.service.register(Nom, Cognoms, Nom_Usuari, Password, ProfilePicturePath, Curs, Experiencia)
                if (resposta.isSuccessful) {
                    _register.value = resposta.body()
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
                _registerLoading.value = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel() as T
    }
}