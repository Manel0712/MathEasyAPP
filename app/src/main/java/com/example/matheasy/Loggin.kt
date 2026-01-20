package com.example.matheasy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import com.example.matheasy.databinding.ActivityLogginBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.viewModels.LogginViewModel
import com.example.matheasy.viewModels.LogginViewModelFactory
import com.google.android.material.snackbar.Snackbar

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
            }
            else {
                binding.progress.visibility = View.GONE
            }
        }
        viewModel.loggin.observe(this) { alumnes ->
            if (alumnes.size > 0) {
                alumne.Nom = alumnes[0].Nom
                alumne.Cogmons = alumnes[0].Cogmons
                alumne.Password = alumnes[0].Password
                alumne.ProfilePicturePath = alumnes[0].ProfilePicturePath
                alumne.Nom_Usuari = alumnes[0].Nom_Usuari
                alumne.Curs = alumnes[0].Curs
                alumne.Experiencia = alumnes[0].Experiencia
            }
        }
        viewModel.error.observe(this) {
            if (it != null) {
                val snackbar = Snackbar.make(binding.root, it,
                    Snackbar.LENGTH_LONG).setAction("Action", null)
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
}