package com.example.matheasy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.matheasy.databinding.ActivityNivellsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Array
import kotlin.random.Random
import kotlin.text.toDouble
import android.speech.tts.TextToSpeech
import java.util.Locale

class nivells : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var reproducirAlEstarListo = false
    private lateinit var binding: ActivityNivellsBinding
    private lateinit var nivell: String
    private lateinit var numero: String
    private lateinit var progressBar: ProgressBar
    var contador: Int = 0
    var operacions = Array(10) { Array(5) { "" } }
    var amagat = 0
    var opcioCorrecta = 0
    var resposta = 0
    var respostesCorrectes = 0
    var desbloqueado = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNivellsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.Resultados.visibility = View.GONE
        binding.Explicacion.visibility = View.VISIBLE
        tts = TextToSpeech(this, this)
        progressBar = binding.progressBar
        progressBar.max = operacions.size
        progressBar.progress = 0
        nivell = intent.getStringExtra("nivell").toString()
        numero = intent.getStringExtra("numero").toString()
        val layoutExplicacion = binding.Explicacion
        val layoutResultados = binding.Resultados
        layoutExplicacion.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (v.visibility == View.VISIBLE) {
                reproducirAlEstarListo = true
            } else {
                if (ttsReady) tts.stop()
            }
        }
        layoutResultados.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (v.visibility == View.VISIBLE && ttsReady) {
                AnunciarResultados()
            } else if (v.visibility != View.VISIBLE && ttsReady) {
                tts.stop()
            }
        }
        if (numero.equals("1-1")) {
            binding.textView.text = nivell + "\n\nResol les següents sumes triant el resultat o el número que falta a l'espai en blanc"
        }
        else if (numero.equals("1-2")) {
            binding.textView.text = nivell + "\n\nResol les següents restes triant el resultat o el número que falta a l'espai en blanc"
        }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("ca"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            if (reproducirAlEstarListo) {
                leerDescripcion()
                reproducirAlEstarListo = false
            }
        }
    }
    private fun leerDescripcion() {
        if (numero.equals("1-1")) {
            val texto = "Resol les següents sumes triant el resultat o el número que falta a l'espai en blanc"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
        else if (numero.equals("1-2")) {
            val texto = "Resol les següents restes triant el resultat o el número que falta a l'espai en blanc"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
    }
    private fun AnunciarResultados() {
        if (respostesCorrectes>=5) {
            val texto = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
        else {
            val texto = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
    fun jugarClick(view: View) {
        if (numero.equals("1-1")) {
            binding.Explicacion.visibility = View.GONE
            binding.world1level1.visibility = View.VISIBLE
            val operacionesUnicas = mutableSetOf<Pair<Int, Int>>()
            var c = 0

            while (c < 10) {
                val numero1 = Random.nextInt(0, 10)
                val numero2 = Random.nextInt(0, 10)
                val key = Pair(numero1, numero2)

                if (key in operacionesUnicas) continue

                operacionesUnicas.add(key)

                val numero3 = numero1 + numero2
                operacions[c][0] = numero1.toString()
                operacions[c][1] = "+"
                operacions[c][2] = numero2.toString()
                operacions[c][3] = "="
                operacions[c][4] = numero3.toString()

                c++
            }
        }
        else if (numero.equals("1-2")) {
            binding.Explicacion.visibility = View.GONE
            binding.world1level1.visibility = View.VISIBLE
            val operacionesUnicas = mutableSetOf<Pair<Int, Int>>()
            var c = 0

            while (c < 10) {
                val numero1 = Random.nextInt(0, 10)
                val numero2 = Random.nextInt(0, 10)
                if (numero1 - numero2 < 0) continue
                val key = Pair(numero1, numero2)

                if (key in operacionesUnicas) continue

                operacionesUnicas.add(key)

                val numero3 = numero1 - numero2
                operacions[c][0] = numero1.toString()
                operacions[c][1] = "-"
                operacions[c][2] = numero2.toString()
                operacions[c][3] = "="
                operacions[c][4] = numero3.toString()

                c++
            }
        }
        novaOperacio()
    }
    fun novaOperacio() {
        binding.textView2.text = ""
        binding.textView3.text = ""
        binding.textView4.text = ""
        binding.textView5.text = ""
        binding.textView6.text = ""
        binding.textView2.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.textView3.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.textView4.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.textView5.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.textView6.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.textView7.setTypeface(null, Typeface.NORMAL)
        binding.textView8.setTypeface(null, Typeface.NORMAL)
        binding.textView9.setTypeface(null, Typeface.NORMAL)
        amagat = Random.nextInt(1, 4)
        if (amagat==1) {
            resposta = operacions[contador][0].toInt()
            binding.textView3.text = operacions[contador][1]
            binding.textView4.text = operacions[contador][2]
            binding.textView5.text = operacions[contador][3]
            binding.textView6.text = operacions[contador][4]
            opcioCorrecta = Random.nextInt(1, 4)
            val correct = operacions[contador][0].toInt()
            var random1: Int
            var random2: Int

            do {
                random1 = Random.nextInt(0, 10)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 10)
            } while (random2 == correct || random2 == random1)
            if (opcioCorrecta==1) {
                binding.textView7.text = operacions[contador][0]
                binding.textView8.text = random1.toString()
                binding.textView9.text = random2.toString()
            }
            else if (opcioCorrecta==2) {
                binding.textView7.text = random1.toString()
                binding.textView8.text = operacions[contador][0]
                binding.textView9.text = random2.toString()
            }
            else {
                binding.textView7.text = random1.toString()
                binding.textView8.text = random2.toString()
                binding.textView9.text = operacions[contador][0]
            }
        }
        else if (amagat==2) {
            resposta = operacions[contador][2].toInt()
            binding.textView2.text = operacions[contador][0]
            binding.textView3.text = operacions[contador][1]
            binding.textView5.text = operacions[contador][3]
            binding.textView6.text = operacions[contador][4]
            opcioCorrecta = Random.nextInt(1, 4)
            val correct = operacions[contador][2].toInt()
            var random1: Int
            var random2: Int

            do {
                random1 = Random.nextInt(0, 10)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 10)
            } while (random2 == correct || random2 == random1)
            if (opcioCorrecta==1) {
                binding.textView7.text = operacions[contador][2]
                binding.textView8.text = random1.toString()
                binding.textView9.text = random2.toString()
            }
            else if (opcioCorrecta==2) {
                binding.textView7.text = random1.toString()
                binding.textView8.text = operacions[contador][2]
                binding.textView9.text = random2.toString()
            }
            else {
                binding.textView7.text = random1.toString()
                binding.textView8.text = random2.toString()
                binding.textView9.text = operacions[contador][2]
            }
        }
        else {
            resposta = operacions[contador][4].toInt()
            binding.textView2.text = operacions[contador][0]
            binding.textView3.text = operacions[contador][1]
            binding.textView4.text = operacions[contador][2]
            binding.textView5.text = operacions[contador][3]
            opcioCorrecta = Random.nextInt(1, 4)
            val correct = operacions[contador][4].toInt()
            var random1: Int
            var random2: Int

            do {
                random1 = Random.nextInt(0, 10)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 10)
            } while (random2 == correct || random2 == random1)
            if (opcioCorrecta==1) {
                binding.textView7.text = operacions[contador][4]
                binding.textView8.text = random1.toString()
                binding.textView9.text = random2.toString()
            }
            else if (opcioCorrecta==2) {
                binding.textView7.text = random1.toString()
                binding.textView8.text = operacions[contador][4]
                binding.textView9.text = random2.toString()
            }
            else {
                binding.textView7.text = random1.toString()
                binding.textView8.text = random2.toString()
                binding.textView9.text = operacions[contador][4]
            }
        }
    }
    fun option1Click(view: View) {
        binding.textView7.setTypeface(null, Typeface.BOLD)
        binding.textView8.setTypeface(null, Typeface.NORMAL)
        binding.textView9.setTypeface(null, Typeface.NORMAL)
        if (amagat==1) {
            binding.textView2.text = binding.textView7.text
        }
        else if (amagat==2) {
            binding.textView4.text = binding.textView7.text
        }
        else {
            binding.textView6.text = binding.textView7.text
        }
    }
    fun option2Click(view: View) {
        binding.textView7.setTypeface(null, Typeface.NORMAL)
        binding.textView8.setTypeface(null, Typeface.BOLD)
        binding.textView9.setTypeface(null, Typeface.NORMAL)
        if (amagat==1) {
            binding.textView2.text = binding.textView8.text
        }
        else if (amagat==2) {
            binding.textView4.text = binding.textView8.text
        }
        else {
            binding.textView6.text = binding.textView8.text
        }
    }
    fun option3Click(view: View) {
        binding.textView7.setTypeface(null, Typeface.NORMAL)
        binding.textView8.setTypeface(null, Typeface.NORMAL)
        binding.textView9.setTypeface(null, Typeface.BOLD)
        if (amagat==1) {
            binding.textView2.text = binding.textView9.text
        }
        else if (amagat==2) {
            binding.textView4.text = binding.textView9.text
        }
        else {
            binding.textView6.text = binding.textView9.text
        }
    }
    fun comprovarResultat(view: View) {
        lifecycleScope.launch {
            if (amagat == 1) {
                if (binding.textView2.text.toString().toInt() == resposta) {
                    binding.textView2.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    respostesCorrectes = respostesCorrectes + 1
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                } else {
                    binding.textView2.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaIncorrecta
                        )
                    )
                    delay(2000)
                    binding.textView2.text = resposta.toString()
                    binding.textView2.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                }
            } else if (amagat == 2) {
                if (binding.textView4.text.toString().toInt() == resposta) {
                    binding.textView4.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    respostesCorrectes = respostesCorrectes + 1
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                } else {
                    binding.textView4.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaIncorrecta
                        )
                    )
                    delay(2000)
                    binding.textView4.text = resposta.toString()
                    binding.textView4.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                }
            } else {
                if (binding.textView6.text.toString().toInt() == resposta) {
                    binding.textView6.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    respostesCorrectes = respostesCorrectes + 1
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                } else {
                    binding.textView6.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaIncorrecta
                        )
                    )
                    delay(2000)
                    binding.textView6.text = resposta.toString()
                    binding.textView6.setTextColor(
                        ContextCompat.getColor(
                            this@nivells,
                            R.color.respostaCorrecta
                        )
                    )
                    delay(2000)
                    contador = contador + 1
                    progressBar.setProgress(contador, true)
                    if (contador == 10) {
                        binding.world1level1.visibility = View.GONE
                        binding.Resultados.visibility = View.VISIBLE
                        if (respostesCorrectes>=5) {
                            binding.textView10.text = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                            desbloqueado = true
                        }
                        else {
                            binding.textView10.text = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                            binding.textView12.text = "Errades\n\n" + (10-respostesCorrectes).toString()
                        }
                    } else {
                        novaOperacio()
                    }
                }
            }
        }
    }
    fun close(view: View) {
        val i: Intent = Intent()
        i.putExtra("bloqueado", desbloqueado)
        i.putExtra("numero", numero)
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}