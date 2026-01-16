package com.example.matheasy

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matheasy.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
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
        val imatge: ImageView = binding.imageView
        val logo = generaImatge()
        imatge.setImageBitmap(logo)
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
        startActivity(i)
    }
}