package com.example.matheasy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matheasy.databinding.ActivityEntregaTascaBinding
import com.example.matheasy.models.Tasca
import com.example.matheasy.models.operacions

class EntregaTasca : AppCompatActivity() {
    private lateinit var binding: ActivityEntregaTascaBinding
    private lateinit var operacions: List<operacions>
    private lateinit var respostes: MutableList<Int>
    var tascaId: Int = 0
    var nOperacio: Int = 0
    var estatTramesa: String = ""
    var nota: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEntregaTascaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        estatTramesa = (intent.getSerializableExtra("operacions") as Tasca).pivot.Estat_tramesa
        if (estatTramesa.equals("No entregat")) {
            operacions = (intent!!.getSerializableExtra("operacions") as Tasca).operacions
            tascaId = (intent.getSerializableExtra("operacions") as Tasca).id
            respostes = mutableListOf()
            novaOperacio()
        }
        else {
            binding.Resultat.visibility = View.GONE
            binding.Resultados.visibility = View.VISIBLE
            binding.textView12.text = "No Qualificada"
            if (estatTramesa.equals("Qualificada")) {
                nota = (intent.getSerializableExtra("operacions") as Tasca).pivot.Qualificacio!!
                binding.textView12.text = nota.toString() + "/10"
            }
        }
    }
    fun novaOperacio() {
        binding.textInputEditText7.setText("")
        binding.textView25.text = operacions[nOperacio].Operacio + " ="
    }
    fun següentOperacio(view: View) {
        respostes.add(binding.textInputEditText7.text.toString().toInt())
        nOperacio++
        if (nOperacio<10) {
            novaOperacio()
        }
        else {
            binding.Resultat.visibility = View.GONE
            binding.Resultados.visibility = View.VISIBLE
            binding.textView12.text = "No Qualificada"
        }
    }
    fun close(view: View) {
        val i: Intent = Intent()
        if (estatTramesa.equals("No entregat")) {
            var resultats = ArrayList(respostes)
            i.putIntegerArrayListExtra("resultats", resultats)
        }
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}