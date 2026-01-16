package com.example.matheasy

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
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
import com.example.matheasy.models.LocationItem
import com.example.matheasy.models.MarkerInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var mapa: GoogleMap

    private val marques: MutableList<Marker?> = ArrayList()

    private var isPanelOpen = false
    private var panelWidth = 0f
    private var niveles = getLocations()

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

        val appInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")
        Log.d("API_KEY", "Mi API Key: " + apiKey ?: "Null")

        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = LocationAdapter(getLocations()) { location ->
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
            val tagInfo = clickedMarker.tag as? MarkerInfo ?: return@setOnInfoWindowClickListener
            val i = Intent(this, nivells::class.java)
            i.putExtra("nivell", tagInfo.title)
            i.putExtra("numero", tagInfo.numero)
            resultLauncherConfiguration.launch(i)
        }
    }

    var resultLauncherConfiguration = registerForActivityResult(ActivityResultContracts.StartActivityForResult())

    { result ->
        val data: Intent? = result.data
        if (data!!.getStringExtra("numero").toString().equals("1-1")) {
            if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[1].bloqueado = false
                niveles[0].completado = true
                binding.rvLocations.adapter = LocationAdapter(niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 2) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            }
        }
        else if (data.getStringExtra("numero").toString().equals("1-2")) {
            mapa.clear()
            for (c in 0 until 2) {
                niveles[1].completado = true
                if (!niveles[c].bloqueado) {
                    marcarUbicacion(niveles[c])
                }
            }
            mostrarDialogoAceptar(this,"Si vols seguir jugant has de registrar-te")
            /* if (data!!.getBooleanExtra("bloqueado", false)) {
                mapa.clear()
                niveles[1].bloqueado = false
                niveles[0].completado = true
                binding.rvLocations.adapter = LocationAdapter(niveles) { location ->
                    marcarUbicacion(location)
                    cerrarPanel()
                }
                binding.rvLocations.adapter!!.notifyDataSetChanged()
                for (c in 0 until 2) {
                    if (!niveles[c].bloqueado) {
                        marcarUbicacion(niveles[c])
                    }
                }
            } */
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
        if (location.completado) {
            marker = mapa.addMarker(
                MarkerOptions().position(latLng).anchor(0.5f, 1f).icon(markerIconFromDrawable(this,R.drawable.ic_marker))
            )
        }
        else {
            marker = mapa.addMarker(
                MarkerOptions().position(latLng).anchor(0.5f, 1f)
            )
        }

        marker?.tag = MarkerInfo(
            title = location.name,
            numero = location.numero
        )

        marker?.let { marques.add(it) }

        marker?.setAnchor(0.5f, 1f)

        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 14f)
        )
    }

    private fun getLocations(): List<LocationItem> {
        return listOf(
            LocationItem("Mataró", 41.5354924, 2.4456584, "1-1", false),
            LocationItem("Ocata", 41.4831877, 2.3317447, "1-2"),
        )
    }
}