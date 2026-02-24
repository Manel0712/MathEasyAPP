package com.example.matheasy

import android.R.attr.width
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.autofill.Validators.or
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.matheasy.adapters.LocationAdapter
import com.example.matheasy.adapters.infoWindowAdapter
import com.example.matheasy.databinding.ActivityMapaBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.models.LocationItem
import com.example.matheasy.models.MarkerInfo
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.os.postDelayed
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.Callback
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import kotlin.collections.listOf
import java.util.Collections.addAll
import androidx.core.os.HandlerCompat
import com.example.matheasy.models.EstacionInfo
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Mapa : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapaBinding
    private lateinit var mapa: GoogleMap
    private lateinit var mapa3DFragment: Map3DView
    private lateinit var mapa3D: GoogleMap3D
    private val marques: MutableList<Marker?> = ArrayList()
    private var isPanelOpen = false
    private var panelWidth = 0f
    private lateinit var niveles: List<LocationItem>
    private lateinit var alumne: Alumne
    var convidats = false
    val estacionesMarcadas = HashSet<String>()

    private lateinit var apiKey: String
    private lateinit var infoView: View
    private lateinit var titulo: TextView
    private lateinit var subtitulo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        convidats = intent.getBooleanExtra("convidats", false)
        if (!convidats) {
            alumne = intent.getSerializableExtra("Alumne") as Alumne
        }

        niveles = getLocations()

        val appInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")!!
        Log.d("API_KEY", "Mi API Key: " + apiKey ?: "Null")

        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = LocationAdapter(this, getLocations()) { location ->
            marcarUbicacion(location)
            cerrarPanel()
        }

        binding.btnTogglePanel.setOnClickListener {
            if (isPanelOpen) cerrarPanel() else abrirPanel()
        }

        val mapaFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        binding.rvLocations.post {
            panelWidth = binding.rvLocations.width.toFloat()
            abrirPanel()
        }
    }

    override fun onMapReady(mapaGoogle: GoogleMap) {
        mapa = mapaGoogle
        mapa.setInfoWindowAdapter(infoWindowAdapter(this))

        mapa.setOnInfoWindowClickListener { clickedMarker ->
            val tagInfo = clickedMarker.tag
            if (tagInfo !is MarkerInfo) return@setOnInfoWindowClickListener
            val i = Intent(this, nivells::class.java)
            i.putExtra("nivell", tagInfo.title)
            i.putExtra("numero", tagInfo.numero)
            i.putExtra("convidats", convidats)
            if (!convidats) {
                i.putExtra("Alumne", alumne)
            }
            resultLauncherConfiguration.launch(i)
        }

        for (c in 1 until niveles.size) {
            if (niveles[c-1].completado) {
                val origen = LatLng(niveles[c-1].lat, niveles[c-1].lng)
                val destino = LatLng(niveles[c].lat, niveles[c].lng)
                obtenerRuta(origen, destino)
            }
        }
    }
    private fun obtenerRuta(origen: LatLng, destino: LatLng) {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origen.latitude},${origen.longitude}" +
                    "&destination=${destino.latitude},${destino.longitude}" +
                    "&mode=transit" +
                    "&transit_mode=subway|train|tram" +
                    "&key=$apiKey"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MAPS", "Error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                procesarRuta(JSONObject(json))
            }
        })
    }
    private fun procesarRuta(json: JSONObject) {
        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return
        val steps = routes.getJSONObject(0)
            .getJSONArray("legs")
            .getJSONObject(0)
            .getJSONArray("steps")
        val rutaCompleta = ArrayList<LatLng>()
        for (i in 0 until steps.length()) {
            val step = steps.getJSONObject(i)
            val points = step.getJSONObject("polyline").getString("points")
            val coords = decodePoly(points)
            runOnUiThread {
                if (step.getString("travel_mode") == "WALKING") {
                    dibujarCaminando(coords)
                } else if (step.has("transit_details")) {
                    dibujarTransporte(step, coords)
                }
            }
            rutaCompleta.addAll(coords)
        }
        if (rutaCompleta.isNotEmpty()) {
            runOnUiThread {
                mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(rutaCompleta.first(), 14f))
            }
        }
    }
    private fun dibujarCaminando(coords: List<LatLng>) {
        mapa.addPolyline(
            PolylineOptions()
                .addAll(coords)
                .width(20f)
                .color(Color.GRAY)
                .pattern(listOf(Dash(30f), Gap(20f)))
        )
    }
    private fun dibujarTransporte(step: JSONObject, coords: List<LatLng>) {
        val transit = step.getJSONObject("transit_details")
        val line = transit.getJSONObject("line")
        val colorHex = line.optString("color", "#2196F3")
        val color = Color.parseColor(colorHex)
        val shortName = line.optString("short_name", "")
        val vehicle = line.getJSONObject("vehicle").getString("type")
        mapa.addPolyline(
        PolylineOptions()
            .addAll(coords)
            .width(20f)
            .color(color)
        )
        Log.d("TRANSIT", "$vehicle - Línea $shortName")
    }
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do { b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }

    var resultLauncherConfiguration = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        val data: Intent? = result.data
        if (!convidats) {
            alumne = data!!.getSerializableExtra("alumne") as Alumne
        }
        if (data!!.getStringExtra("numero").toString().equals("1-1")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[1].bloqueado = false
                niveles[0].completado = true
                binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 6) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-2")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[2].bloqueado = false
                niveles[1].completado = true
                binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 6) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-3")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[3].bloqueado = false
                niveles[2].completado = true
                binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 6) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-4")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                if (convidats) {
                    mapa.clear()
                    for (c in 0 until 4) {
                        niveles[3].completado = true
                        if (!niveles[c].bloqueado) {
                            marcarUbicacion(niveles[c])
                        }
                    }
                    mostrarDialogoAceptar(this, "Si vols seguir jugant has de registrar-te")
                }
                else {
                    mapa.clear()
                    niveles[4].bloqueado = false
                    niveles[3].completado = true
                    binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                        marcarUbicacion(location)
                        cerrarPanel()
                    }
                    binding.rvLocations.adapter!!.notifyDataSetChanged()
                    for (c in 0 until 6) {
                        if (!niveles[c].bloqueado) {
                            marcarUbicacion(niveles[c])
                        }
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-5")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[5].bloqueado = false
                niveles[4].completado = true
                binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 6) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-6")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                /*mapa.clear()
                niveles[6].bloqueado = false
                niveles[5].completado = true
                binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 5) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }*/
                mapa.clear()
                for (c in 0 until 6) {
                    niveles[5].completado = true
                    binding.rvLocations.adapter = LocationAdapter(this, niveles) { location ->
                        marcarUbicacion(location)
                        cerrarPanel()
                    }
                    binding.rvLocations.adapter!!.notifyDataSetChanged()
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
                mostrarDialogoAceptar(this, "Mes nivells properament")
            }
        }
    }

    fun markerIconFromDrawable(context: Context, drawableId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!

        // Tamaño exacto del vector (24dp)
        val scale = context.resources.displayMetrics.density
        val size = (48 * scale).toInt()
        drawable.setBounds(0, 0, size, size)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun mostrarDialogoAceptar(context: Context, mensaje: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Información")
        builder.setMessage(mensaje)

        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun abrirPanel() {
        if (isPanelOpen) return

        binding.rvLocations.animate()
            .translationX(0f)
            .setDuration(300)
            .start()

        binding.btnTogglePanel.animate()
            .translationX(0f)
            .setDuration(300)
            .start()

        binding.btnTogglePanel.setImageResource(R.drawable.ic_arrow_right)
        isPanelOpen = true
    }

    private fun cerrarPanel() {
        if (!isPanelOpen) return

        binding.rvLocations.animate()
            .translationX(panelWidth)
            .setDuration(300)
            .start()

        binding.btnTogglePanel.animate()
            .translationX(panelWidth)
            .setDuration(300)
            .start()

        binding.btnTogglePanel.setImageResource(R.drawable.ic_arrow_left)
        isPanelOpen = false
    }

    private fun marcarUbicacion(location: LocationItem) {
        val latLng = LatLng(location.lat, location.lng)

        var marker: Marker?
        if (location.completado && !location.numero.equals("1-6")) {
            marker = mapa.addMarker(
                MarkerOptions().position(latLng).anchor(0.5f, 1f).icon(markerIconFromDrawable(this,R.drawable.ic_marker))
            )
        }
        else if (location.numero.equals("1-6")) {
            if (location.completado) {
                marker = mapa.addMarker(
                    MarkerOptions().position(latLng).anchor(0.5f, 1f).icon(markerIconFromDrawable(this,R.drawable.castillo__2_))
                )
            }
            else {
                marker = mapa.addMarker(
                    MarkerOptions().position(latLng).anchor(0.5f, 1f).icon(markerIconFromDrawable(this,R.drawable.castillo__1_))
                )
            }
        }
        else {
            marker = mapa.addMarker(
                MarkerOptions().position(latLng).anchor(0.5f, 1f)
            )
        }

        marker?.tag = MarkerInfo(
            title = location.name,
            numero = location.numero,
            image = location.image,
        )

        marker?.let { marques.add(it) }

        marker?.setAnchor(0.5f, 1f)

        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 14f)
        )
    }

    private fun getLocations(): List<LocationItem> {
        if (convidats) {
            return listOf(
                LocationItem("Mataró", 41.5354924, 2.4456584, "1-1", "p1010255", false),
                LocationItem("Ocata", 41.4831877, 2.3317447, "1-2", "aldi_el_masnou"),
                LocationItem("Badalona", 41.4490912, 2.2423281, "1-3", "estacio_de_badalona_pompeu_fabra"),
                LocationItem("Hospitalet", 41.3554322, 2.1246428, "1-4", "mwc_barcelona"),
                LocationItem("Barcelona-Sud", 41.3898803, 2.1153956, "1-5", "marenostrum_supercomputador_bsc_barcelona"),
                LocationItem("Barcelona-Centre", 41.3871144, 2.1684571, "1-6", "corteinglesplacacatalunya")
            )
        }
        else {
            var locations = listOf(
                LocationItem("Mataró", 41.5354924, 2.4456584, "1-1", "p1010255"),
                LocationItem("Ocata", 41.4831877, 2.3317447, "1-2", "aldi_el_masnou"),
                LocationItem("Badalona", 41.4490912, 2.2423281, "1-3", "estacio_de_badalona_pompeu_fabra"),
                LocationItem("Hospitalet", 41.3554322, 2.1246428, "1-4", "mwc_barcelona"),
                LocationItem("Barcelona-Sud", 41.3898803, 2.1153956, "1-5", "marenostrum_supercomputador_bsc_barcelona"),
                LocationItem("Barcelona-Centre", 41.3871144, 2.1684571, "1-6", "corteinglesplacacatalunya")
            )
            for (c in 0 .. alumne.Nivell) {
                if (c==0) {
                    locations[c].bloqueado = false
                }
                else if (c != locations.size) {
                    locations[c-1].completado = true
                    locations[c].bloqueado = false
                }
                else {
                    locations[c-1].completado = true
                }
            }
            return locations
        }
    }

    fun closeMapa(view: View) {
        val i: Intent = Intent()
        if (!convidats) {
            i.putExtra("alumne", alumne)
        }
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}