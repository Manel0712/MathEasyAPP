package com.example.matheasy.viewModels

import kotlinx.coroutines.launch
import androidx.lifecycle.*
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.DownloadBase64Image
import com.example.matheasy.retrofit.Connection
import java.io.IOException
import retrofit2.Response

class LogginViewModel: ViewModel() {
    private val _logginLoading = MutableLiveData(false)
    public val logginLoading: LiveData<Boolean> get() = _logginLoading

    private val _loggin = MutableLiveData<List<Alumne>>(emptyList())
    public val loggin: LiveData<List<Alumne>> get() = _loggin

    private val _profileImage = MutableLiveData<List<DownloadBase64Image>>(emptyList())
    public val profileImage: LiveData<List<DownloadBase64Image>> get() = _profileImage

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun loggin(Nom_Usuari: String, Password: String) {
        viewModelScope.launch {
            _logginLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<Alumne>>
                resposta = Connection.service.loggin(Nom_Usuari, Password)
                if (resposta.isSuccessful) {
                    _loggin.value = resposta.body()
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
                _logginLoading.value = false
            }
        }
    }

    public fun profilePictureDownload(path: String) {
        viewModelScope.launch {
            _logginLoading.value = true
            _error.value = null

            try {
                lateinit var resposta: Response<List<DownloadBase64Image>>
                resposta = Connection.service.downloadBase64Image(path)
                if (resposta.isSuccessful) {
                    _profileImage.value = resposta.body()
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
                _logginLoading.value = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class LogginViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return LogginViewModel() as T
    }
}