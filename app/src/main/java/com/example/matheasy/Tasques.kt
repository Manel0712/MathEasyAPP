package com.example.matheasy

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.matheasy.adapters.TasquesAdapter
import com.example.matheasy.databinding.ActivityTasquesBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.Tasca
import com.example.matheasy.models.Tema
import com.example.matheasy.viewModels.LogginViewModelFactory
import com.example.matheasy.viewModels.TemesViewModel
import com.example.matheasy.viewModels.TemesViewModelFactory
import com.google.android.material.snackbar.Snackbar

class Tasques : AppCompatActivity() {

    private lateinit var binding: ActivityTasquesBinding
    private val viewModel: TemesViewModel by viewModels { TemesViewModelFactory() }
    private lateinit var alumne: Alumne
    private val recyclerViews = mutableListOf<RecyclerView>()
    private lateinit var tema: List<Tema>
    var temaId: Int = 0
    var tascaId: Int = 0
    var respostes: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityTasquesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.temaLoading.observe(this) { cargando ->
            if (cargando) {
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.visibility = View.GONE
            }
        }

        viewModel.tema.observe(this) { temes ->

            tema = temes

            binding.containerTemes.removeAllViews()

            temes.forEach { tema ->

                // TITULO DEL TEMA
                val title = TextView(this)
                title.text = tema.Nom
                title.textSize = 20f
                title.setPadding(20,20,20,20)

                binding.containerTemes.addView(title)

                // RECYCLERVIEW DE TAREAS
                val recycler = RecyclerView(this)

                recycler.layoutManager =
                    LinearLayoutManager(this)

                recycler.adapter =
                    TasquesAdapter(tema.tasques, {message(it)})

                recycler.visibility = View.GONE

                recyclerViews.add(recycler)

                binding.containerTemes.addView(recycler)

                // CLICK PARA EXPANDIR
                title.setOnClickListener {

                    recyclerViews.forEach {
                        it.visibility = View.GONE
                    }

                    recycler.visibility =
                        if (recycler.visibility == View.GONE)
                            View.VISIBLE
                        else
                            View.GONE
                }
            }
        }

        viewModel.entrega.observe(this) { entrega ->
            if (entrega.size>0) {
                viewModel.temes(alumne.id)
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

        alumne = intent!!.getSerializableExtra("Alumne") as Alumne
        viewModel.temes(alumne.id)
    }
    fun message(tasca: Tasca) {
        temaId = tasca.tema
        tascaId = tasca.id
        val i = Intent(this, EntregaTasca::class.java)
        i.putExtra("operacions", tasca)
        resultLauncherConfiguration.launch(i)
    }

    var resultLauncherConfiguration = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        var temaSeleccionado = tema.find { it.id == temaId }
        var tascaSeleccionada = temaSeleccionado!!.tasques.find { it.id == tascaId }
        if (tascaSeleccionada!!.pivot.Estat_tramesa.equals("No entregat")) {
            val data: Intent? = result.data
            respostes = data!!.getIntegerArrayListExtra("resultats")!!.toMutableList()
            viewModel.entregues(alumne.Email, tascaSeleccionada.pivot.id, "Entregada", respostes[0], respostes[1], respostes[2], respostes[3], respostes[4], respostes[5], respostes[6], respostes[7], respostes[8], respostes[9])
        }
    }
}