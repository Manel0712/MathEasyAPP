package com.example.matheasy

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.matheasy.databinding.ActivityMain2Binding
import com.example.matheasy.models.Alumne

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var alumne: Alumne
    var convidats = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        convidats = intent.getBooleanExtra("convidats", false)
        val imatge: ImageView = binding.imageView
        if (convidats) {
            val logo = generaImatge()
            imatge.setImageBitmap(logo)
        }
        else {
            alumne = intent.getSerializableExtra("alumne") as Alumne
            val image = "http://10.0.2.2:8000/storage/${alumne.ProfilePicturePath}"
            Glide.with(this)
                .load(image)
                .dontTransform()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imageView)
            binding.buttonPerfil.visibility = View.VISIBLE
        }
    }
    private fun generaImatge(): Bitmap {
        val width = 500
        val height = 500

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val color = resources.getColor(R.color.fons, null)
        canvas.drawColor(color)

        val paint = Paint()
        paint.color = resources.getColor(R.color.black, null)
        paint.textSize = 300f
        paint.isAntiAlias = true

        val poppins: Typeface? = ResourcesCompat.getFont(this, R.font.poppins_bold)
        paint.typeface = poppins

        val text = "C"
        val textWidth = paint.measureText(text)
        val x = (width - textWidth) / 2
        val y = (height - (paint.descent() + paint.ascent())) / 2

        canvas.drawText(text, x, y, paint)
        return bitmap
    }
    fun practicaClick(view: View) {
        val i = Intent(this, Mapa::class.java)
        i.putExtra("convidats", convidats)
        if (!convidats) {
            i.putExtra("Alumne", alumne)
        }
        startActivity(i)
    }
    fun perfilClick(view: View) {
        val i = Intent(this, Perfil::class.java)
        i.putExtra("Alumne", alumne)
        resultLauncherConfiguration.launch(i)
    }
    var resultLauncherConfiguration = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        val data: Intent? = result.data
        alumne = data!!.getSerializableExtra("alumne") as Alumne
        val image = "http://10.0.2.2:8000/storage/${alumne.ProfilePicturePath}"
        Glide.with(this)
            .load(image)
            .dontTransform()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageView)
    }
}