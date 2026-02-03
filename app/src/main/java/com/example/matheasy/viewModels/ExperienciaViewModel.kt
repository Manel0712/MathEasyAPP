package com.example.matheasy.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.Experiencia
import com.example.matheasy.models.ImageUpload
import com.example.matheasy.models.Informe
import com.example.matheasy.retrofit.Connection
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class ExperienciaViewModel: ViewModel() {
    private val _experiencia = MutableLiveData<List<Experiencia>>(emptyList())
    public val experiencia: LiveData<List<Experiencia>> get() = _experiencia

    private val _experienciaUpdate = MutableLiveData<List<Experiencia>>(emptyList())
    public val experienciaUpdate: LiveData<List<Experiencia>> get() = _experienciaUpdate

    private val _level = MutableLiveData<List<Alumne>>(emptyList())
    public val level: LiveData<List<Alumne>> get() = _level

    private val _informe = MutableLiveData<List<Informe>>(emptyList())
    public val informe: LiveData<List<Informe>> get() = _informe

    private val _error = MutableLiveData<String?>(null)
    public val error: LiveData<String?> get() = _error

    public fun experiencia(experiencia: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                lateinit var resposta: Response<List<Experiencia>>
                resposta = Connection.service.experiencia(experiencia)
                if (resposta.isSuccessful) {
                    _experiencia.value = resposta.body()
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

    public fun experienciaUpdate(experiencia: Int, Nivell: Int, Total_xp: Int, Medalles: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                lateinit var resposta: Response<List<Experiencia>>
                resposta = Connection.service.experienciaUpdate(experiencia, Nivell, Total_xp, Medalles)
                if (resposta.isSuccessful) {
                    _experienciaUpdate.value = resposta.body()
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

    public fun editLevel(alumne: Int, Nivell: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                lateinit var resposta: Response<List<Alumne>>
                resposta = Connection.service.editLevel(alumne, Nivell)
                if (resposta.isSuccessful) {
                    _level.value = resposta.body()
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

    public fun informeGenerated(Tipus_partida: String, Respostes_correctes: Int, Respostes_incorrectes: Int, Experiencia: Int, Alumne: Int) {
        viewModelScope.launch {
            _error.value = null

            try {
                lateinit var resposta: Response<List<Informe>>
                resposta = Connection.service.informeCreated(Tipus_partida, Respostes_correctes, Respostes_incorrectes, Experiencia, Alumne)
                if (resposta.isSuccessful) {
                    _informe.value = resposta.body()
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
class ExperienciaViewModelFactory(): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return ExperienciaViewModel() as T
    }
}