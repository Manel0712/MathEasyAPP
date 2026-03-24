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
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.matheasy.databinding.ActivityMain2Binding
import com.example.matheasy.models.Alumne
import com.example.matheasy.viewModels.EditDiaryViewModel
import com.example.matheasy.viewModels.EditDiaryViewModelFactory

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var alumne: Alumne
    private val viewModel: EditDiaryViewModel by viewModels { EditDiaryViewModelFactory() }
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
        viewModel.edit.observe(this) { experiencia ->
            if (experiencia.size > 0) {
                alumne.experiencia.Nivell = experiencia[0].Nivell
                alumne.experiencia.Total_xp = experiencia[0].Total_xp
                alumne.experiencia.Medalles = experiencia[0].Medalles
            }
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
            binding.buttonCompetitiu.visibility = View.VISIBLE
            binding.buttonInformes.visibility = View.VISIBLE
            binding.buttonPerfil.visibility = View.VISIBLE
            mostrarOperacionDiariaSiNoVista(alumne.id.toString())
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
    private fun mostrarOperacionDiariaSiNoVista(User: String) {
        val prefs = getSharedPreferences("operacion_diaria_prefs", Context.MODE_PRIVATE)
        val ultimaFecha = prefs.getString(User, null)
        val hoy = java.time.LocalDate.now().toString()

        if (ultimaFecha != hoy) {
            // Si no se ha mostrado hoy, mostrar el diálogo
            mostrarDialogoOperacionDiariaConRespuesta()

            // Guardar que ya se mostró hoy
            prefs.edit().putString(User, java.time.LocalDate.now().toString()).apply()
        }
    }
    private fun generarOperacionDiaria(): String {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // días desde epoch
        val seed = today.toInt()
        val random = java.util.Random(seed.toLong())

        val a = random.nextInt(20) + 1  // 1 a 20
        val b = random.nextInt(20) + 1  // 1 a 20
        val operadores = listOf("+")

        val operador = operadores[random.nextInt(operadores.size)]

        return "$a $operador $b"
    }
    private fun mostrarDialogoOperacionDiariaConRespuesta() {
        val operacion = generarOperacionDiaria()

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Operación del Día")
            .setMessage(operacion)
            .setView(input)
            .setPositiveButton("Comprobar") { dialog, _ ->
                val respuestaUsuario = input.text.toString().toIntOrNull()
                val respuestaCorrecta = calcularResultado(operacion)
                if (respuestaUsuario == respuestaCorrecta) {
                    Toast.makeText(this, "¡Correcto! 🎉", Toast.LENGTH_SHORT).show()
                    viewModel.editExperiencia(alumne.experiencia.id, alumne.experiencia.Total_xp+20, alumne.experiencia.Nivell, alumne.experiencia.Medalles)
                } else {
                    Toast.makeText(this, "Incorrecto, la respuesta era $respuestaCorrecta", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun calcularResultado(operacion: String): Int {
        val parts = operacion.split(" ")
        val a = parts[0].toInt()
        val b = parts[2].toInt()
        return when(parts[1]) {
            "+" -> a + b
            else -> 0
        }
    }
    fun practicaClick(view: View) {
        val i = Intent(this, Mapa::class.java)
        i.putExtra("convidats", convidats)
        if (!convidats) {
            i.putExtra("Alumne", alumne)
        }
        resultLauncherConfiguration2.launch(i)
    }
    fun competituClick(view: View) {
        val i = Intent(this, ModeCompetitiu::class.java)
        i.putExtra("Alumne", alumne)
        startActivity(i)
    }
    fun informesClick(view: View) {
        val i = Intent(this, Informes::class.java)
        i.putExtra("Alumne", alumne)
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
    var resultLauncherConfiguration2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
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