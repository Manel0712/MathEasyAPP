package com.example.matheasy

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.biometric.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.example.matheasy.databinding.ActivityLogginBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.viewModels.LogginViewModel
import com.example.matheasy.viewModels.LogginViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class Loggin : AppCompatActivity() {
    private lateinit var binding: ActivityLogginBinding
    private val viewModel: LogginViewModel by viewModels { LogginViewModelFactory() }
    private lateinit var alumne: Alumne
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.logginLoading.observe(this) { cargando ->
            if (cargando) {
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.visibility = View.GONE
            }
        }
        viewModel.loggin.observe(this) { alumnes ->
            if (alumnes != null) {
                var token = alumnes.Resposta.token
                alumne = alumnes.Resposta.Alumne
                saveToken(this, token!!)
                val i = Intent(this, MainActivity2::class.java)
                i.putExtra("convidats", false)
                i.putExtra("alumne", alumne)
                startActivity(i)
            }
        }
        viewModel.biometricLoggin.observe(this) { alumnes ->
            if (alumnes.size > 0) {
                alumne = alumnes[0]
                val i = Intent(this, MainActivity2::class.java)
                i.putExtra("convidats", false)
                i.putExtra("alumne", alumne)
                startActivity(i)
            }
        }
        viewModel.error.observe(this) {
            if (it != null) {
                val snackbar = Snackbar.make(
                    binding.root, it,
                    Snackbar.LENGTH_LONG
                ).setAction("Action", null)
                snackbar.setActionTextColor(Color.WHITE)
                val snackbarView = snackbar.view
                snackbarView.setBackgroundColor(Color.RED)
                val textView =
                    snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setTextColor(Color.WHITE)
                textView.textSize = 28f
                snackbar.show()
            }
        }
    }
    fun loggin(view: View) {
        var Usuari = binding.textInputEditText.text
        var Password = binding.textInputEditText2.text
        viewModel.loggin(Usuari.toString(), Password.toString())
    }

    fun isEmulator(): Boolean {
        return (Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("google") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("goldfish"))
    }

    fun checkBiometric(view: View) {
        if (isEmulator()) {
            // Emulador → entrar directo
            showSnackbar("Emulador: login automático")

            viewModel.biometricLoggin(getToken(this)!!)
        }
        showBiometricPrompt()
    }

    fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)

        // Consultamos qué métodos biométricos están disponibles
        val canAuthenticateStrong = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )

        val canAuthenticateWeak = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        // Esto es solo para demostración, se puede priorizar solo si el dispositivo tiene cara
        if (canAuthenticateStrong == BiometricManager.BIOMETRIC_SUCCESS ||
            canAuthenticateWeak == BiometricManager.BIOMETRIC_SUCCESS) {

            // Intentamos crear un prompt
            val executor = ContextCompat.getMainExecutor(this)

            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        showSnackbar("Autenticación facial exitosa")
                        // Aquí llamas a tu función de login con token
                        viewModel.biometricLoggin(getToken(this@Loggin)!!)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        showSnackbar("Error: $errString")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showSnackbar("Autenticación fallida")
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login con reconocimiento facial")
                .setSubtitle("Pon tu cara frente a la cámara")
                .setNegativeButtonText("Cancelar")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            showSnackbar("No hay método facial disponible")
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun saveToken(context: Context, token: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString("TOKEN", token).apply()
    }

    private fun getToken(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString("TOKEN", null)
    }

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            "secure_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}