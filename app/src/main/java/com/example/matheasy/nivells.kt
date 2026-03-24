package com.example.matheasy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.icu.text.Transliterator
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
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
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.transition.Visibility
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.MoneyData
import com.example.matheasy.viewModels.ExperienciaViewModel
import com.example.matheasy.viewModels.ExperienciaViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.ParticleSystem
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import kotlin.getValue

class nivells : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var reproducirAlEstarListo = false
    private lateinit var binding: ActivityNivellsBinding
    private val viewModel: ExperienciaViewModel by viewModels { ExperienciaViewModelFactory() }
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
    var nivelXP: Int = 1
    var totalXP: Int = 0
    var dailyXP: Int = 0
    var XP: Int = 0
    val xpPorAcierto = 10
    private lateinit var alumne: Alumne
    var convidats = false
    private var selectedLeft: TextView? = null
    private var selectedRight: TextView? = null
    private val usedLefts = mutableSetOf<TextView>()
    private val usedRights = mutableSetOf<TextView>()
    data class Conexion(val left: TextView, val right: TextView)
    private val lineasDibujadas = mutableListOf<Conexion>()
    private lateinit var frameLayout: FrameLayout
    private lateinit var depositZone: ImageView
    private lateinit var totalText: TextView
    private var totalAmount = 0
    private var money = 0
    private val allMoney = mutableListOf<MoneyData>()
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
        viewModel.experienciaUpdate.observe(this) { experiencia ->
            if (experiencia.size > 0) {
                if (respostesCorrectes >= 5 && numero.split("-")[1].toInt()-1 == alumne.Nivell) {
                    viewModel.editLevel(alumne.id, alumne.Nivell + 1)
                }
                else {
                    viewModel.editLevel(alumne.id, alumne.Nivell)
                }
            }
        }
        viewModel.level.observe(this) { alumnes ->
            if (alumnes.size > 0) {
                alumne = alumnes[0]
                viewModel.informeGenerated("Individual", respostesCorrectes, 10-respostesCorrectes, XP, alumne.id)
            }
        }
        viewModel.informe.observe(this) { informe ->
            if (informe.size > 0) {
                val snackbar = Snackbar.make(
                    binding.root, "Experiencia emmagatzemada correctament",
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
        binding.Resultados.visibility = View.GONE
        binding.Explicacion.visibility = View.VISIBLE
        tts = TextToSpeech(this, this)
        progressBar = binding.progressBar
        progressBar.max = operacions.size
        progressBar.progress = 0
        nivell = intent.getStringExtra("nivell").toString()
        numero = intent.getStringExtra("numero").toString()
        convidats = intent.getBooleanExtra("convidats", false)
        if (!convidats) {
            alumne = intent.getSerializableExtra("Alumne") as Alumne
            nivelXP = alumne.experiencia.Nivell
            totalXP = alumne.experiencia.Total_xp
            dailyXP = totalXP - xpNecesariaHastaNivel(nivelXP)
        }
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
        if (numero.equals("1-1") || numero.equals("1-3")) {
            binding.textView.text = nivell + "\n\nResol les següents sumes triant el resultat o el número que falta a l'espai en blanc"
        }
        else if (numero.equals("1-2") || numero.equals("1-4")) {
            binding.textView.text = nivell + "\n\nResol les següents restes triant el resultat o el número que falta a l'espai en blanc"
        }
        else if (numero.equals("1-5")) {
            binding.textView.text = nivell + "\n\nIntrodueix el nombre de monedes i bitllets necesaris perque al moneder hi hagi la cuantitat de diners demanada"
        }
        else if (numero.equals("1-6")) {
            binding.textView.text = nivell + "\n\nRelaciona les següents divisions amb el resultat corresponent"
        }
    }
    private fun xpNecesariaHastaNivel(nivel: Int): Int {
        var xp = 0
        for (i in 1 until nivel) {
            xp += (100 * Math.pow(i.toDouble(), 1.5)).toInt()
        }
        return xp
    }
    private fun xpToNextLevel(): Int = (100 * Math.pow(nivelXP.toDouble(), 1.5)).toInt()
    private fun addXp(amount: Int) {
        dailyXP += amount
        totalXP += amount
        XP += amount
        while (dailyXP >= xpToNextLevel()) {
            dailyXP -= xpToNextLevel()
            nivelXP++
            playLevelUpAnimation()
            playLevelUpSound()
        }
    }
    private fun playLevelUpSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 500)
    }

    private fun playCorrectSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
    }

    private fun playIncorrectSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 300)
    }
    private fun playLevelUpAnimation() {
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.YELLOW)
        val centerX = binding.konfettiView.width / 2f

        binding.konfettiView.build()
            .addColors(*colors)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.RECT, Shape.CIRCLE)
            .addSizes(Size(12))
            .setPosition(
                centerX, centerX, // centro horizontal
                0f, 0f             // parte superior
            )
            .streamFor(300, 5000L)
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
        if (numero.equals("1-1") || numero.equals("1-3")) {
            val texto = "Resol les següents sumes triant el resultat o el número que falta a l'espai en blanc"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
        else if (numero.equals("1-2") || numero.equals("1-4")) {
            val texto = "Resol les següents restes triant el resultat o el número que falta a l'espai en blanc"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
        else if (numero.equals("1-5")) {
            val texto = "Introdueix el nombre de monedes i bitllets necesaris perque al moneder hi hagi la cuantitat de diners demanada"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
        else if (numero.equals("1-6")) {
            val texto = "Relaciona les següents divisions amb el resultat corresponent"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "descripcionID")
        }
    }
    private fun AnunciarResultados() {
        if (respostesCorrectes>=5 && !numero.equals("1-5")) {
            val texto = "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
        else if (respostesCorrectes>=5 && !numero.equals("1-5")) {
            val texto = "Felicitats, has encertat un total de " + respostesCorrectes + " problemes i has desbloquejat el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
        else if (numero.equals("1-5")) {
            val texto = "Mala sort, nomes has encertat " + respostesCorrectes + " problemes i has d'encertar 5 problemes per desbloquejar el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
        else {
            val texto = "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
        }
        if (!convidats) {
            if (respostesCorrectes==10) {
                viewModel.experienciaUpdate(alumne.experiencia.id, nivelXP, totalXP, alumne.experiencia.Medalles+1)
            }
            else {
                viewModel.experienciaUpdate(alumne.experiencia.id, nivelXP, totalXP, alumne.experiencia.Medalles)
            }
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

            novaOperacio()
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

            novaOperacio()
        }
        else if (numero.equals("1-3")) {
            binding.Explicacion.visibility = View.GONE
            binding.world1level1.visibility = View.VISIBLE
            val operacionesUnicas = mutableSetOf<Pair<Int, Int>>()
            var c = 0

            while (c < 10) {
                val numero1 = Random.nextInt(0, 20)
                val numero2 = Random.nextInt(0, 20)
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

            novaOperacio2()
        }
        else if (numero.equals("1-4")) {
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

                val numero3 = numero1 - numero2
                operacions[c][0] = numero1.toString()
                operacions[c][1] = "-"
                operacions[c][2] = numero2.toString()
                operacions[c][3] = "="
                operacions[c][4] = numero3.toString()

                c++
            }

            novaOperacio()
        }
        else if (numero.equals("1-5")) {
            binding.Explicacion.visibility = View.GONE
            binding.world1level5.visibility = View.VISIBLE
            /* val operacionesUnicas = mutableSetOf<Pair<Int, Int>>()
            var c = 0

            while (c < 10) {
                val numero1 = Random.nextInt(0, 10)
                val numero2 = Random.nextInt(0, 10)
                val key = Pair(numero1, numero2)

                if (key in operacionesUnicas) continue

                operacionesUnicas.add(key)

                val numero3 = numero1 * numero2
                operacions[c][0] = numero1.toString()
                operacions[c][1] = "*"
                operacions[c][2] = numero2.toString()
                operacions[c][3] = "="
                operacions[c][4] = numero3.toString()

                c++
            }

            novaOperacio()*/
            frameLayout = binding.world1level5
            depositZone = binding.imageView10
            money = Random.nextInt(1, 20)

            // Texto que muestra el total
            totalText = binding.textView17.apply {
                text = "$money€"
                textSize = 24f
                setTextColor(android.graphics.Color.BLACK)
            }

            val texto = "Introdueix $money€ al moneder"
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")

            val bills = listOf(
                Pair(R.drawable.billete_5, 5)
            )

            val coins = listOf(
                Pair(R.drawable.moneda_1, 1),
                Pair(R.drawable.moneda_2, 2)
            )

            val startY = 80f // posición superior de la pantalla
            var startX = 50f // empezar desde la izquierda
            val spacingX = 250f // separación horizontal entre tipos

            // Crear 10 billetes de cada tipo, apilados
            for ((drawable, value) in bills) {
                createMoney(drawable, value, startX, startY)
                startX += spacingX
            }

            // Crear 10 monedas de cada tipo, apilados
            for ((drawable, value) in coins) {
                createMoney(drawable, value, startX, startY)
                startX += spacingX
            }
        }
        else if (numero.equals("1-6")) {
            binding.Explicacion.visibility = View.GONE
            binding.world1level6.visibility = View.VISIBLE
            /*val operacionesUnicas = mutableSetOf<Pair<Int, Int>>()
            var c = 0

            while (c < 10) {
                val numero1 = Random.nextInt(0, 10)
                val numero2 = Random.nextInt(1, 10)
                val key = Pair(numero1, numero2)

                if (key in operacionesUnicas) continue

                if (numero1 % numero2 != 0) continue

                operacionesUnicas.add(key)

                val numero3 = numero1 / numero2
                operacions[c][0] = numero1.toString()
                operacions[c][1] = "/"
                operacions[c][2] = numero2.toString()
                operacions[c][3] = "="
                operacions[c][4] = numero3.toString()

                c++
            }

            novaOperacio()*/
            generarDivisiones()
            setupTextViews()
        }
    }
    private fun createMoney(drawableRes: Int, value: Int, baseX: Float, baseY: Float) {
        val stackCount = 10
        val topMargin = 150f
        val offsetY = 20f // mayor separación vertical por el tamaño más grande

        for (i in 0 until stackCount) {
            val money = ImageView(this)
            money.setImageResource(drawableRes)

            // Tamaño más grande
            val width = 350
            val height = 225
            money.layoutParams = FrameLayout.LayoutParams(width, height)

            // Posición original (apilados)
            val originalX = baseX
            val originalY = topMargin + i * offsetY // apilados hacia arriba
            val originalZ = i * offsetY
            money.x = originalX
            money.y = originalY
            money.translationZ = originalZ

            frameLayout.addView(money)

            var insideZone = false

            val moneyData = MoneyData(
                money,
                originalX,
                originalY,
                originalZ,
                value
            )

            allMoney.add(moneyData)

            money.setOnTouchListener(object : View.OnTouchListener {
                var dX = 0f
                var dY = 0f

                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            dX = view.x - event.rawX
                            dY = view.y - event.rawY
                            view.bringToFront()
                            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            view.x = event.rawX + dX
                            view.y = event.rawY + dY

                            val moneyRect = Rect()
                            val zoneRect = Rect()
                            view.getHitRect(moneyRect)
                            depositZone.getHitRect(zoneRect)

                            val nowInside = Rect.intersects(moneyRect, zoneRect)

                            // Solo usamos insideDeposit de moneyData
                            if (nowInside && !moneyData.insideDeposit) {
                                totalAmount += moneyData.value
                                moneyData.insideDeposit = true
                            } else if (!nowInside && moneyData.insideDeposit) {
                                totalAmount -= moneyData.value
                                moneyData.insideDeposit = false
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                            val moneyRect = Rect()
                            val zoneRect = Rect()
                            view.getHitRect(moneyRect)
                            depositZone.getHitRect(zoneRect)

                            val nowInside = Rect.intersects(moneyRect, zoneRect)
                            if (!nowInside) {
                                // Volver a la posición original visualmente
                                view.animate()
                                    .x(moneyData.originalX)
                                    .y(moneyData.originalY)
                                    .translationZ(moneyData.originalZ)
                                    .setDuration(300)
                                    .start()
                                // totalAmount ya se ajusta en ACTION_MOVE
                                // insideDeposit se mantiene false hasta que se vuelva a arrastrar
                            }
                        }
                    }
                    return true
                }
            })
        }
    }
    fun comprovarMoneder(view: View) {
        lifecycleScope.launch {
            if (totalAmount == money) {
                playCorrectSound()
                binding.imageView10.setBackgroundColor(
                    ContextCompat.getColor(
                        this@nivells,
                        R.color.respostaCorrecta
                    )
                )
                addXp(xpPorAcierto)
                delay(2000)
                respostesCorrectes = respostesCorrectes + 1
                contador = contador + 1
                if (contador == 10) {
                    binding.world1level5.visibility = View.GONE
                    binding.Resultados.visibility = View.VISIBLE
                    if (respostesCorrectes >= 5) {
                        binding.textView10.text =
                            "Felicitats, has encertat un total de " + respostesCorrectes + " problemes i has desbloquejat el següent nivell"
                        binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                        binding.textView12.text =
                            "Errades\n\n" + (10 - respostesCorrectes).toString()
                        desbloqueado = true
                    } else {
                        binding.textView10.text =
                            "Mala sort, nomes has encertat " + respostesCorrectes + " problemes i has d'encertar 5 operacions per desbloquejar el següent nivell"
                        binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                        binding.textView12.text =
                            "Errades\n\n" + (10 - respostesCorrectes).toString()
                    }
                } else {
                    reiniciarMonedes()

                    binding.imageView10.background = null

                    money = Random.nextInt(1, 20)

                    totalText = binding.textView17.apply {
                        text = "$money€"
                        textSize = 24f
                        setTextColor(android.graphics.Color.BLACK)
                    }

                    val texto = "Introdueix $money€ al moneder"
                    tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
                }
            } else {
                playIncorrectSound()
                binding.imageView10.setBackgroundColor(
                    ContextCompat.getColor(
                        this@nivells,
                        R.color.respostaIncorrecta
                    )
                )
                delay(2000)
                posarQuantitatExacta(money)
                binding.imageView10.setBackgroundColor(
                    ContextCompat.getColor(
                        this@nivells,
                        R.color.respostaCorrecta
                    )
                )
                delay(2000)
                contador = contador + 1
                if (contador == 10) {
                    binding.world1level5.visibility = View.GONE
                    binding.Resultados.visibility = View.VISIBLE
                    if (respostesCorrectes >= 5) {
                        binding.textView10.text =
                            "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
                        binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                        binding.textView12.text =
                            "Errades\n\n" + (10 - respostesCorrectes).toString()
                        desbloqueado = true
                    } else {
                        binding.textView10.text =
                            "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
                        binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
                        binding.textView12.text =
                            "Errades\n\n" + (10 - respostesCorrectes).toString()
                    }
                } else {
                    reiniciarMonedes()

                    binding.imageView10.background = null

                    money = Random.nextInt(1, 20)

                    totalText = binding.textView17.apply {
                        text = "$money€"
                        textSize = 24f
                        setTextColor(android.graphics.Color.BLACK)
                    }

                    val texto = "Introdueix $money€ al moneder"
                    tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "resultatsID")
                }
            }
        }
    }
    private fun organizarDeposito() {

        val billetes = allMoney.filter { it.insideDeposit && it.value >= 5 }
        val monedas2 = allMoney.filter { it.insideDeposit && it.value == 2 }
        val monedas1 = allMoney.filter { it.insideDeposit && it.value == 1 }

        val depositCenterX = depositZone.x + depositZone.width / 2f
        val depositCenterY = depositZone.y + depositZone.height / 2f

        val maxSpacingX = 50f // separación horizontal
        val spacingY = 50f    // separación vertical entre filas

        // -------------------
        // BILLETES (IZQUIERDA, desde el centro hacia la izquierda)
        // -------------------
        if (billetes.isNotEmpty()) {
            val startXBilletes = depositCenterX - 20f // empieza un poco a la izquierda del centro
            val posYBilletes = depositCenterY - spacingY

            billetes.forEachIndexed { index, money ->
                val posX = startXBilletes - index * maxSpacingX // mover cada billete hacia la izquierda
                money.view.animate()
                    .x(posX)
                    .y(posYBilletes)
                    .setDuration(300)
                    .start()
            }
        }

        // -------------------
        // MONEDAS 2€ (DERECHA ARRIBA)
        // -------------------
        if (monedas2.isNotEmpty()) {
            val startX2 = depositCenterX + 20f // un poco a la derecha del centro
            val posY2 = depositCenterY - spacingY

            monedas2.forEachIndexed { index, money ->
                val posX = startX2 + index * maxSpacingX
                money.view.animate()
                    .x(posX)
                    .y(posY2)
                    .setDuration(300)
                    .start()
            }
        }

        // -------------------
        // MONEDAS 1€ (DERECHA ABAJO, separadas verticalmente)
        // -------------------
        if (monedas1.isNotEmpty()) {
            val startX1 = depositCenterX + 20f
            val posY1 = depositCenterY + spacingY // espacio vertical respecto a las de 2€

            monedas1.forEachIndexed { index, money ->
                val posX = startX1 + index * maxSpacingX
                money.view.animate()
                    .x(posX)
                    .y(posY1)
                    .setDuration(300)
                    .start()
            }
        }
    }
    fun posarQuantitatExacta(cantidad: Int) {

        reiniciarMonedes()

        var restante = cantidad

        // Ordenar de mayor a menor valor
        val sortedMoney = allMoney.sortedByDescending { it.value }

        for (money in sortedMoney) {

            if (!money.insideDeposit && money.value <= restante) {

                // Asignar zona según tipo
                val (posX, posY) = when {
                    money.value >= 5 -> { // BILLETES
                        val index = allMoney.filter { it.insideDeposit && it.value >= 5 }.size
                        val startX = depositZone.x + 20f
                        val startY = depositZone.y + 20f
                        startX + index * 35f to startY
                    }
                    money.value == 2 -> { // MONEDAS 2€
                        val index = allMoney.filter { it.insideDeposit && it.value == 2 }.size
                        val startX = depositZone.x + depositZone.width * 0.55f
                        val startY = depositZone.y + 20f
                        startX + index * 35f to startY
                    }
                    money.value == 1 -> { // MONEDAS 1€
                        val index = allMoney.filter { it.insideDeposit && it.value == 1 }.size
                        val startX = depositZone.x + depositZone.width * 0.55f
                        val startY = depositZone.y + 140f
                        startX + index * 35f to startY
                    }
                    else -> {
                        // Valores raros, centrado
                        depositZone.x + depositZone.width / 2f - money.view.width / 2f to
                                depositZone.y + depositZone.height / 2f - money.view.height / 2f
                    }
                }

                money.view.animate()
                    .x(posX)
                    .y(posY)
                    // Mantener Z original
                    .setDuration(300)
                    .start()

                money.insideDeposit = true
                restante -= money.value
                totalAmount += money.value
            }

            if (restante == 0) break
        }

        organizarDeposito()

        // Si no se pudo formar exactamente, deshacer
        if (restante != 0) {
            reiniciarMonedes()
        }
    }
    fun reiniciarMonedes() {
        totalAmount = 0
        for (moneyData in allMoney) {
            moneyData.view.animate()
                .x(moneyData.originalX)
                .y(moneyData.originalY)
                .translationZ(moneyData.originalZ)
                .setDuration(300)
                .start()
            moneyData.insideDeposit = false
        }
    }
    fun novaOperacio() {
        binding.textView13.text = "Respostes correctes: " + respostesCorrectes + "/10"
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
        binding.textView7.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView8.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView9.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.imageView4.visibility = View.VISIBLE
        binding.imageView4.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            rotation = 0f
            translationX = 0f
            translationY = 0f
        }
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
    fun novaOperacio2() {
        binding.textView13.text = "Respostes correctes: " + respostesCorrectes + "/10"
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
        binding.textView7.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView8.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView9.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.imageView4.visibility = View.VISIBLE
        binding.imageView4.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            rotation = 0f
            translationX = 0f
            translationY = 0f
        }
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
                random1 = Random.nextInt(0, 20)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 20)
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
                random1 = Random.nextInt(0, 20)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 20)
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
                random1 = Random.nextInt(0, 20)
            } while (random1 == correct)

            do {
                random2 = Random.nextInt(0, 20)
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
        binding.textView7.setBackgroundColor(ContextCompat.getColor(this, R.color.fons))
        binding.textView8.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView9.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
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
        binding.textView7.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView8.setBackgroundColor(ContextCompat.getColor(this, R.color.fons))
        binding.textView9.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
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
        binding.textView7.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView8.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.textView9.setBackgroundColor(ContextCompat.getColor(this, R.color.fons))
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
        if (numero.equals("1-1") || numero.equals("1-2") || numero.equals("1-4")) {
            comprovarResultatNovaOperacio()
        }
        else if (numero.equals("1-3")) {
            comprovarResultatNovaOperacio2()
        }
    }
    fun comprovarResultatNovaOperacio() {
        lifecycleScope.launch {
            if (amagat == 1) {
                if (binding.textView2.text.toString().toInt() == resposta) {
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
    fun comprovarResultatNovaOperacio2() {
        lifecycleScope.launch {
            if (amagat == 1) {
                if (binding.textView2.text.toString().toInt() == resposta) {
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                        novaOperacio2()
                    }
                } else {
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
                        novaOperacio2()
                    }
                }
            } else if (amagat == 2) {
                if (binding.textView4.text.toString().toInt() == resposta) {
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                        novaOperacio2()
                    }
                } else {
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
                        novaOperacio2()
                    }
                }
            } else {
                if (binding.textView6.text.toString().toInt() == resposta) {
                    playCorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-100f)  // sube 100px
                        .setDuration(300)       // duración del salto
                        .withEndAction {
                            binding.imageView4.animate()
                                .translationYBy(100f)  // baja de nuevo
                                .setDuration(300)
                                .start()
                        }
                        .start()
                    if (!convidats) {
                        addXp(xpPorAcierto)
                    }
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
                        novaOperacio2()
                    }
                } else {
                    playIncorrectSound()
                    binding.imageView4.animate()
                        .translationYBy(-50f)
                        .setDuration(200)
                        .withEndAction {
                            binding.imageView4.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .rotationBy(720f)
                                .setDuration(400)
                                .withEndAction {
                                    binding.imageView4.visibility = View.GONE
                                }
                                .start()
                        }
                        .start()
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
                        novaOperacio2()
                    }
                }
            }
        }
    }
    fun close(view: View) {
        val i: Intent = Intent()
        i.putExtra("bloqueado", desbloqueado)
        i.putExtra("numero", numero)
        i.putExtra("convidats", convidats)
        if (!convidats) {
            i.putExtra("alumne", alumne)
        }
        setResult(Activity.RESULT_OK, i)
        finish()
    }
    fun abandonar(view: View) {
        showConfirmDialog(
            this,
            "Confirmació",
            "¿Estas segur que vols abandonar la partida?",
            onYes = {
                val i: Intent = Intent()
                i.putExtra("bloqueado", desbloqueado)
                i.putExtra("numero", numero)
                i.putExtra("convidats", convidats)
                if (!convidats) {
                    i.putExtra("alumne", alumne)
                }
                setResult(Activity.RESULT_OK, i)
                finish()
            }
        )
    }
    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        onYes: () -> Unit
    ) {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)

        builder.setPositiveButton("Sí") { dialog, _ ->
            onYes()
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        dialog.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }

        dialog.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(context.getColor(R.color.fons))
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(context.getColor(R.color.fons))
    }
    fun generarDivisiones() {
        val operaciones = mutableListOf<Pair<String, Int>>() // Pair<operación, resultado>

        while (operaciones.size < 10) { // ahora 10 operaciones
            val divisor = Random.nextInt(1, 10)
            val cociente = Random.nextInt(1, 10)
            val dividendo = divisor * cociente

            val operacionStr = "$dividendo / $divisor" // izquierda
            if (operaciones.none { it.first == operacionStr }) {
                operaciones.add(Pair(operacionStr, cociente)) // derecha = resultado
            }
        }

        // Asignar a los TextViews dinámicamente
        val leftViews = listOf(
            binding.textLeft1, binding.textLeft2, binding.textLeft3,
            binding.textLeft4, binding.textLeft5, binding.textLeft6,
            binding.textLeft7, binding.textLeft8, binding.textLeft9, binding.textLeft10
        )

        val rightViews = listOf(
            binding.textRight1, binding.textRight2, binding.textRight3,
            binding.textRight4, binding.textRight5, binding.textRight6,
            binding.textRight7, binding.textRight8, binding.textRight9, binding.textRight10
        )

        leftViews.forEachIndexed { index, tv ->
            tv.text = operaciones[index].first
        }

        val resultados = operaciones.map { it.second }.shuffled()
        rightViews.forEachIndexed { index, tv ->
            tv.text = resultados[index].toString()
        }
    }

    private fun setupTextViews() {
        val leftViews = listOf(
            binding.textLeft1, binding.textLeft2, binding.textLeft3,
            binding.textLeft4, binding.textLeft5, binding.textLeft6,
            binding.textLeft7, binding.textLeft8, binding.textLeft9, binding.textLeft10
        )

        val rightViews = listOf(
            binding.textRight1, binding.textRight2, binding.textRight3,
            binding.textRight4, binding.textRight5, binding.textRight6,
            binding.textRight7, binding.textRight8, binding.textRight9, binding.textRight10
        )

        leftViews.forEach { tv ->
            tv.setOnClickListener { selectLeft(tv) }
        }

        rightViews.forEach { tv ->
            tv.setOnClickListener { selectRight(tv) }
        }
    }

    private fun selectLeft(tv: TextView) {
        if (usedLefts.contains(tv)) return // ya unido → ignorar

        selectedLeft?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        selectedLeft = tv
        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.fons))

        if (selectedRight != null) drawLineBetweenSelected()
    }

    private fun selectRight(tv: TextView) {
        if (usedRights.contains(tv)) return // ya unido → ignorar

        selectedRight?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        selectedRight = tv
        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.fons))

        if (selectedLeft != null) drawLineBetweenSelected()
    }

    fun drawLineBetweenSelected() {
        if (selectedLeft != null && selectedRight != null) {

            // Comprobar si alguno ya está conectado
            if (usedLefts.contains(selectedLeft!!) || usedRights.contains(selectedRight!!)) return

            // Guardar los lados como usados
            usedLefts.add(selectedLeft!!)
            usedRights.add(selectedRight!!)

            lineasDibujadas.add(Conexion(selectedLeft!!, selectedRight!!))

            // Posiciones en pantalla
            val start = IntArray(2)
            val end = IntArray(2)
            selectedLeft!!.getLocationOnScreen(start)
            selectedRight!!.getLocationOnScreen(end)

            val startX = start[0] + selectedLeft!!.width / 2f
            val startY = start[1] + selectedLeft!!.height / 2f
            val endX = end[0] + selectedRight!!.width / 2f
            val endY = end[1] + selectedRight!!.height / 2f

            // Dibujar línea en LineView
            binding.linesView.addLine(Pair(startX, startY), Pair(endX, endY))

            // Limpiar selección
            selectedLeft = null
            selectedRight = null
        }
    }

    fun comprobarTodas(view: View) {
        var aciertos = 0

        for (conexion in lineasDibujadas) {
            val parts = conexion.left.text.toString().split(" / ")
            val dividendo = parts[0].toInt()
            val divisor = parts[1].toInt()
            val resultado = conexion.right.text.toString().toInt()

            if (dividendo / divisor == resultado) {
                aciertos++
                respostesCorrectes++
                addXp(xpPorAcierto)
            }
        }

        if (aciertos == lineasDibujadas.size) {
            playCorrectSound()
        } else {
            playIncorrectSound()
        }
        binding.world1level1.visibility = View.GONE
        binding.Resultados.visibility = View.VISIBLE
        if (respostesCorrectes >= 5) {
            binding.textView10.text =
                "Felicitats, has encertat un total de " + respostesCorrectes + " operacions i has desbloquejat el següent nivell"
            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
            binding.textView12.text = "Errades\n\n" + (10 - respostesCorrectes).toString()
            desbloqueado = true
        } else {
            binding.textView10.text =
                "Mala sort, nomes has encertat " + respostesCorrectes + " operacions i has d'encertar 5 operacions per desbloquejar el següent nivell"
            binding.textView11.text = "Encerts\n\n" + respostesCorrectes.toString()
            binding.textView12.text = "Errades\n\n" + (10 - respostesCorrectes).toString()
        }
    }
}