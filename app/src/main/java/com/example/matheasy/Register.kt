package com.example.matheasy

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.adapters.DropdownAdapter
import com.example.matheasy.databinding.ActivityRegisterBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.viewModels.RegisterViewModel
import com.example.matheasy.viewModels.RegisterViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import kotlin.getValue

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels { RegisterViewModelFactory() }
    private lateinit var alumne: Alumne
    private lateinit var selectorBox: FrameLayout
    private lateinit var tvSelector: TextView
    private lateinit var recyclerOptions: RecyclerView
    private lateinit var mainLayout: ConstraintLayout
    private var popupWindow: PopupWindow? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        selectorBox = binding.dropdownContainer
        tvSelector = binding.tvSelected
        mainLayout = binding.main
        val opciones = listOf(
            "I3",
            "I4",
            "I5",
            "1r Primaria",
            "2n Primaria",
            "3r Primaria",
            "4t Primaria",
            "5è Primaria",
            "6è Primaria",
            "1r ESO",
            "2n ESO",
            "3r ESO",
            "4t ESO",
            "1r Baxillerat",
            "2n Baxillerat",
            "1r GM",
            "2n GM",
            "1r GS",
            "2n GS",
            "1r Carrera",
            "2n Carrera",
            "3r Carrera",
            "4t Carrera",
            "1r Master",
            "2n Master",
            "1r Doctorat",
            "2n Doctorat",
            "3r Doctorat"
        )
        binding.tvSelected.setOnClickListener {
            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_dropdown, null)
            showDropdownCentered(popupView, binding.ivArrow, opciones)
        }
        viewModel.registerLoading.observe(this) { cargando ->
            if (cargando) {
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.visibility = View.GONE
            }
        }
        viewModel.register.observe(this) { alumnes ->
            if (alumnes.size > 0) {
                alumne = alumnes[0]
                val i = Intent(this, MainActivity2::class.java)
                i.putExtra("convidats", false)
                i.putExtra("alumne", alumne)
                startActivity(i)
            }
        }
        viewModel.profilePicture.observe(this) { profilePicture ->
            if (profilePicture != null) {
                viewModel.register(binding.textInputEditText.text.toString(), binding.textInputEditText2.text.toString(), binding.textInputEditText3.text.toString(), binding.textInputEditText8.text.toString(), binding.textInputEditText4.text.toString(), profilePicture.path, tvSelector.text.toString(), 0)
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
        binding.textView5.textSize = 20.0f
        binding.textView5.text = "Confirma el " + binding.textView5.text
    }
    private fun showDropdownCentered(anchor: View, arrow: ImageView, options: List<String>) {
        // Inflar layout
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_dropdown, null)

        // Crear popup
        val popupWindow = PopupWindow(
            popupView,
            200.dpToPx(),  // ancho 200dp
            200.dpToPx(),  // alto 200dp
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.WHITE)) // fondo del popup
            elevation = 10f
        }

        // RecyclerView
        val rvDropdown = popupView.findViewById<RecyclerView>(R.id.rvDropdown)
        rvDropdown.layoutManager = LinearLayoutManager(this)
        rvDropdown.adapter = DropdownAdapter(options) { selected ->
            binding.tvSelected.text = selected
            popupWindow.dismiss()
        }

        // Mostrar centrado
        popupWindow.showAtLocation(anchor.rootView, Gravity.CENTER, 0, 0)

        // Opcional: rotar flecha
        rotateArrow(arrow, true)
        popupWindow.setOnDismissListener {
            rotateArrow(arrow, false)
        }
    }

    // Extensión para convertir dp a px
    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
    private fun rotateArrow(arrow: ImageView, expand: Boolean) {
        val fromDeg = if (expand) 0f else 180f
        val toDeg = if (expand) 180f else 0f
        arrow.animate().rotation(toDeg).setDuration(300).start()
    }
    fun registre(view: View) {
        if (binding.textInputEditText4.text.toString().equals(binding.textInputEditText5.text.toString())) {
            val bitmap = generaImatge(firstLetterUppercase(binding.textInputEditText.text.toString()))
            val base64 = bitmapToBase64(bitmap)
            viewModel.profilePictureUpload(base64, extension = "jpg")
        }
        else {
            val snackbar = Snackbar.make(
                binding.root, "Les contresenyes no coincideixen",
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
    fun firstLetterUppercase(text: String): String {
        return text.trim()
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.uppercase()
            ?: ""
    }
    private fun generaImatge(Initial: String): Bitmap {
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

        val text = Initial
        val textWidth = paint.measureText(text)
        val x = (width - textWidth) / 2
        val y = (height - (paint.descent() + paint.ascent())) / 2

        canvas.drawText(text, x, y, paint)
        return bitmap
    }
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}