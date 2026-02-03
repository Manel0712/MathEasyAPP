package com.example.matheasy

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.matheasy.databinding.ActivityPerfilBinding
import com.example.matheasy.models.Alumne
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.viewModels
import com.example.matheasy.viewModels.EditViewModel
import com.example.matheasy.viewModels.EditViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import kotlin.getValue

class Perfil : AppCompatActivity() {
    val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageUri(it) }
    }
    private lateinit var binding: ActivityPerfilBinding
    private val viewModel: EditViewModel by viewModels { EditViewModelFactory() }
    private lateinit var alumne: Alumne
    private lateinit var base64: String
    private lateinit var extension: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.editLoading.observe(this) { cargando ->
            if (cargando) {
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.visibility = View.GONE
            }
        }
        viewModel.edit.observe(this) { alumnes ->
            if (alumnes.size > 0) {
                alumne = alumnes[0]
                val image = "http://10.0.2.2:8000/storage/${alumne.ProfilePicturePath}"
                Glide.with(this)
                    .load(image)
                    .dontTransform()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.imageView)
                if ((alumne.experiencia.Nivell>=5 && alumne.Nivell<10) || (alumne.experiencia.Medalles>=5 && alumne.experiencia.Medalles<10)) {
                    binding.borderView.setBackgroundResource(R.drawable.oig1)
                }
                else if ((alumne.experiencia.Nivell>=10 && alumne.Nivell<20) || (alumne.experiencia.Medalles>=10 && alumne.experiencia.Medalles<20)) {
                    binding.borderView.setBackgroundResource(R.drawable.oig2)
                }
                else if ((alumne.experiencia.Nivell>=20 && alumne.Nivell<40) || (alumne.experiencia.Medalles>=20 && alumne.experiencia.Medalles<40)) {
                    binding.borderView.setBackgroundResource(R.drawable.oig3)
                }
                else if (alumne.experiencia.Nivell>=40 || alumne.experiencia.Medalles>=40) {
                    binding.borderView.setBackgroundResource(R.drawable.oig4)
                }
            }
        }
        viewModel.profilePicture.observe(this) { profilePicture ->
            if (profilePicture != null) {
                viewModel.edit(alumne.id, binding.textInputEditText.text.toString(), binding.textInputEditText2.text.toString(), binding.textInputEditText3.text.toString(), profilePicture.path, binding.textInputEditText6.text.toString(), alumne.experiencia.id)
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
        alumne = intent.getSerializableExtra("Alumne") as Alumne
        binding.textInputEditText.setText(alumne.Nom)
        binding.textInputEditText2.setText(alumne.Cognoms)
        binding.textInputEditText3.setText(alumne.Nom_Usuari)
        binding.textInputEditText6.setText(alumne.Curs)
        val image = "http://10.0.2.2:8000/storage/${alumne.ProfilePicturePath}"
        Glide.with(this)
            .load(image)
            .dontTransform()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageView)
        if ((alumne.experiencia.Nivell>=5 && alumne.Nivell<10) || (alumne.experiencia.Medalles>=5 && alumne.experiencia.Medalles<10)) {
            binding.borderView.setBackgroundResource(R.drawable.oig1)
        }
        else if ((alumne.experiencia.Nivell>=10 && alumne.Nivell<20) || (alumne.experiencia.Medalles>=10 && alumne.experiencia.Medalles<20)) {
            binding.borderView.setBackgroundResource(R.drawable.oig2)
        }
        else if ((alumne.experiencia.Nivell>=20 && alumne.Nivell<40) || (alumne.experiencia.Medalles>=20 && alumne.experiencia.Medalles<40)) {
            binding.borderView.setBackgroundResource(R.drawable.oig3)
        }
        else if (alumne.experiencia.Nivell>=40 || alumne.experiencia.Medalles>=40) {
            binding.borderView.setBackgroundResource(R.drawable.oig4)
        }
        base64 = ""
        extension = ""
    }
    fun imageChange(view: View) {
        getImage.launch("image/*")
    }
    private fun handleImageUri(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        base64 = bitmapToBase64(bitmap)
        extension = getFileExtension(uri)

        viewModel.profilePictureUpload(alumne.ProfilePicturePath, base64, extension)
    }
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
    private fun getFileExtension(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
    }
    fun saveClick(view: View) {
        viewModel.edit(alumne.id, binding.textInputEditText.text.toString(), binding.textInputEditText2.text.toString(), binding.textInputEditText3.text.toString(), alumne.ProfilePicturePath, binding.textInputEditText6.text.toString(), alumne.experiencia.id)
    }
    fun closeClick(view: View) {
        val i = Intent(this, MainActivity2::class.java)
        i.putExtra("convidats", false)
        i.putExtra("alumne", alumne)
        startActivity(i)
    }
}