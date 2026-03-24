package com.example.matheasy

import android.R.attr.height
import android.R.attr.width
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matheasy.adapters.CodeAdapter
import io.socket.client.Socket
import io.socket.client.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import com.example.matheasy.databinding.ActivityModeCompetitiuBinding
import com.example.matheasy.models.Alumne
import com.google.sceneform_assets.m
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

class ModeCompetitiu : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private lateinit var binding: ActivityModeCompetitiuBinding
    private lateinit var alumne: Alumne
    private val codes = mutableListOf<String>()
    private var index: Int = 0
    private lateinit var textView15: String
    private lateinit var textView16: String
    private lateinit var codeSelected: String
    private var chatActivo = true
    lateinit var inputMensaje: EditText
    lateinit var btnEnviar: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityModeCompetitiuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        textView15 = binding.textView15.text.toString()
        textView16 = binding.textView16.text.toString()
        alumne = intent!!.getSerializableExtra("Alumne") as Alumne
        binding.codis.adapter = CodeAdapter(codes, {connectServer(it)})
        CoroutineScope(Dispatchers.IO).launch {
            connectToHostServer()
        }
        binding.chatInput.setOnEditorActionListener { _, _, _ ->
            sendMessageWithEnter()
            true
        }
        binding.chatToggleBtn.setOnClickListener {
            binding.chatContainer.visibility =
                if (binding.chatContainer.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        inputMensaje = binding.chatInput
        btnEnviar = binding.chatSend
    }
    fun isEmulator(): Boolean {
        return (Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("google") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("goldfish"))
    }

    private fun connectToHostServer() {
        var serverIP = ""
        val serverPort = 3000

        if (isEmulator()) {
            // Si es AVD
            serverIP = "10.0.2.2"
            initSocket(serverIP, serverPort)
        } else {
            // Si es un dispositivo real, buscar por broadcast
            discoverServer { discoveredIP ->
                if (discoveredIP != null) {
                    initSocket(discoveredIP, serverPort)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "No se encontró el servidor en la red", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        //initSocket("https://matheasyweb.onrender.com/", 443)
    }

    private fun initSocket(ip: String, port: Int) {
        try {
            val options = IO.Options()
            options.forceNew = true
            options.reconnection = true

            mSocket = IO.socket("http://$ip:$port", options)
            mSocket.connect()

            mSocket.on("game-code") { args ->
                val code = args[0].toString()
                runOnUiThread {
                    if (!codes.contains(code)) {
                        codes.add(code)
                        binding.codis.adapter!!.notifyDataSetChanged()
                    }
                }
            }

            mSocket.on(Socket.EVENT_DISCONNECT) {
                runOnUiThread {
                    Toast.makeText(this, "Conexión perdida con el servidor", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Función para descubrir servidor en la red local
    private fun discoverServer(callback: (String?) -> Unit) {
        Thread {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true

                val msg = "DISCOVER_GAME_SERVER".toByteArray()
                val packet = DatagramPacket(
                    msg, msg.size,
                    InetAddress.getByName("255.255.255.255"),
                    41234
                )
                socket.send(packet)

                val buffer = ByteArray(1024)
                val response = DatagramPacket(buffer, buffer.size)
                socket.soTimeout = 3000 // esperar 3 segundos
                socket.receive(response)

                val respStr = String(response.data, 0, response.length)
                if (respStr.startsWith("GAME_SERVER:")) {
                    val parts = respStr.split(":")
                    val ip = response.address.hostAddress
                    callback(ip)
                } else {
                    callback(null)
                }

                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }
    private fun connectServer(code: String) {
        val data = JSONObject()
        codeSelected = code
        data.put("code", code)
        data.put("name", alumne.Nom)
        data.put("avatar", "http://127.0.0.1:8000/storage/${alumne.ProfilePicturePath}")
        data.put("alumne_id", alumne.id)
        mSocket.emit("join-game", data)

        mSocket.on("welcome") { args ->
            val data = args[0] as JSONObject
            val msg = data.optString("message")
            runOnUiThread {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }

        mSocket.on("operation-started") { args ->
            val data = args[0] as JSONObject
            val msg = data.optString("message")
            runOnUiThread {
                binding.Lobby.visibility = View.GONE
                binding.Resultat.visibility = View.VISIBLE
                binding.textView25.text = "Operacio 1/10"
            }
        }

        mSocket.on("respuesta_correcta") { args ->
            val data = args[0] as JSONObject
            val points = data.optString("points")
            runOnUiThread {
                binding.Espera.visibility = View.GONE
                binding.Correcte.visibility = View.VISIBLE
                binding.textView15.text = textView15 + "\n" + points + " puntos"
                playCorrectSound()
            }
        }

        mSocket.on("respuesta_incorrecta") { args ->
            val data = args[0] as JSONObject
            val points = data.optString("points")
            runOnUiThread {
                binding.Espera.visibility = View.GONE
                binding.Resultat.visibility = View.GONE
                binding.Incorrecte.visibility = View.VISIBLE
                binding.textView16.text = textView16 + "\n" + points + " puntos"
                playIncorrectSound()
            }
        }

        mSocket.on("next-operation-alert") { args ->
            val data = args[0] as JSONObject
            val msg = data.optString("message")
            runOnUiThread {
                binding.Correcte.visibility = View.GONE
                binding.Incorrecte.visibility = View.GONE
                binding.Resultat.visibility = View.VISIBLE
                index++
                binding.textInputEditText7.setText("")
                binding.textView25.text = "Operacio " + index+1 + "/10"
            }
        }

        mSocket.on("show-podium") { args ->
            val participants = args[0] as JSONArray
            runOnUiThread {
                binding.Correcte.visibility = View.GONE
                binding.Incorrecte.visibility = View.GONE
                binding.podiumItem.visibility = View.VISIBLE
                showMyPosition(participants)
            }
        }

        mSocket.on("chat-status") { args ->
            runOnUiThread {
                val data = args[0] as JSONObject
                val activo = data.getBoolean("activo")
                actualizarEstadoChat(activo)
            }
        }

        mSocket.off("chat-message")
        mSocket.on("chat-message", onNewMessage)
    }

    fun actualizarEstadoChat(activo: Boolean) {
        chatActivo = activo

        inputMensaje.isEnabled = activo
        btnEnviar.isEnabled = activo

        if (!activo) {
            inputMensaje.hint = "Chat desactivado por el profesor"
        } else {
            inputMensaje.hint = "Escribe un mensaje..."
        }
    }

    fun enviarResultat(view: View) {
        val data = JSONObject()
        data.put("code", codeSelected)
        data.put("index", index)
        data.put("answer", binding.textInputEditText7.text.toString().toInt())
        mSocket.emit("submit-answer", data)
        binding.Resultat.visibility = View.GONE
        binding.Espera.visibility = View.VISIBLE
    }

    private fun playCorrectSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
    }

    private fun playIncorrectSound() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 300)
    }

    private fun showMyPosition(participants: JSONArray) {
        val myId = mSocket.id()
        var myPosition = -1
        for (i in 0 until participants.length()) {
            val p = participants.getJSONObject(i)
            if (p.getString("id") == myId) {
                myPosition = i
                break
            }
        }

        if (myPosition != -1) {
            createMedalView(myPosition, binding.medalView)
        }
    }

    private fun createMedalView(position: Int, imageView: ImageView) {
        imageView.post {
            val size = imageView.width.coerceAtMost(imageView.height)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = size / 2f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            val centerX = size / 2f
            val centerY = size / 2f

            if (position in 0..2) {
                paint.style = Paint.Style.FILL
                paint.color = when(position) {
                    0 -> Color.YELLOW
                    1 -> Color.LTGRAY
                    else -> Color.rgb(205,127,50)
                }
                canvas.drawCircle(centerX, centerY, size / 2f, paint)
            }

            val yPos = centerY - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText("${position + 1}", centerX, yPos, textPaint)

            imageView.setImageBitmap(bitmap)
        }
    }

    public fun sendMessage(view: View) {
        val text = binding.chatInput.text.toString().trim()
        if (text.isEmpty()) return

        addMessage(alumne.Nom + " " + alumne.Cognoms, text, self = true)

        val msg = JSONObject().apply {
            put("code", codeSelected)
            put("name", alumne.Nom + " " + alumne.Cognoms)
            put("text", text)
        }
        mSocket.emit("chat-message", msg)

        binding.chatInput.text.clear()
    }

    public fun sendMessageWithEnter() {
        val text = binding.chatInput.text.toString().trim()
        if (text.isEmpty()) return

        addMessage(alumne.Nom + " " + alumne.Cognoms, text, self = true)

        val msg = JSONObject().apply {
            put("code", codeSelected)
            put("name", alumne.Nom + " " + alumne.Cognoms)
            put("text", text)
        }
        mSocket.emit("chat-message", msg)

        binding.chatInput.text.clear()
    }

    private val onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            val name = data.getString("name")
            val text = data.getString("text")
            val emisor = data.getString("senderId")
            if (emisor == mSocket.id()) return@runOnUiThread
            addMessage(name, text, self = false)
        }
    }

    private fun addMessage(name: String, text: String, self: Boolean) {
        val msg = TextView(this).apply {
            this.text = "$name: $text"
            setPadding(10, 6, 10, 6)
            setBackgroundColor(if (self) 0xFF32A0CE.toInt() else 0xFFEFEFEF.toInt())
            setTextColor(if (self) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(6, 6, 6, 6)
            gravity = if (self) Gravity.END else Gravity.START
        }

        binding.chatMessages.addView(msg, params)

        // Scroll automático
        binding.chatScroll.post { binding.chatScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mSocket.isInitialized) {
            mSocket.disconnect()
            mSocket.off()
        }
    }

    fun tancarClick(view: View) {
        val i: Intent = Intent()
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    fun tancarXat(view: View) {
        binding.chatContainer.visibility = View.GONE
    }
}