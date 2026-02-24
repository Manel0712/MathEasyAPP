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
import com.example.matheasy.models.Experiencia
import com.example.matheasy.models.ImageUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditDiaryViewModel: ViewModel() {

    private val _edit = MutableLiveData<List<Experiencia>>(emptyList())
    public val edit: LiveData<List<Experiencia>> get() = _edit

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun editExperiencia(Alumne: Int, Total_xp: Int, Nivell: Int, Medalles: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                lateinit var resposta: Response<List<Experiencia>>
                resposta = Connection.service.experienciaUpdate(Alumne, Nivell, Total_xp, Medalles)
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
        }
    }
}

@Suppress("UNCHECKED_CAST")
class EditDiaryViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return EditDiaryViewModel() as T
    }
}