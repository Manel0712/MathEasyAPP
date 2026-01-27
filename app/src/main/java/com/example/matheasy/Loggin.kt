package com.example.matheasy

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
}