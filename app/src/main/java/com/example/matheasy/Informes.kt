package com.example.matheasy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matheasy.adapters.InformeAdapter
import com.example.matheasy.databinding.ActivityInformesBinding
import com.example.matheasy.models.Alumne
import com.example.matheasy.viewModels.InformesViewModel
import com.example.matheasy.viewModels.InformesViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.FileOutputStream
import kotlin.getValue
import android.print.PrintManager
import androidx.core.content.ContextCompat
import android.graphics.Path


class Informes : AppCompatActivity() {
    private lateinit var binding: ActivityInformesBinding
    private val viewModel: InformesViewModel by viewModels { InformesViewModelFactory() }
    private val informesAdapter = InformeAdapter(emptyList(), this, {imprimirCardView(this, it.Tipus_partida, it.Respostes_correctes, it.Respostes_incorrectes, it.Experiencia)})
    private lateinit var alumne: Alumne
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInformesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.informes.adapter = informesAdapter
        viewModel.informesLoading.observe(this) { cargando ->
            if (cargando) {
                binding.progress.visibility = View.VISIBLE
            } else {
                binding.progress.visibility = View.GONE
            }
        }
        viewModel.informes.observe(this) { informes ->
            if (informes.size > 0) {
                informesAdapter.informes = informes
                informesAdapter.notifyDataSetChanged()
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
        alumne = intent.getSerializableExtra("Alumne") as Alumne
        viewModel.informes(alumne.id)
    }
    fun imprimirCardView(
        context: Context,
        tipus: String,
        correctes: Int,
        incorrectes: Int,
        experiencia: Int
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = object : PrintDocumentAdapter() {

            private var printAttributes: PrintAttributes? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                printAttributes = newAttributes

                callback?.onLayoutFinished(
                    PrintDocumentInfo.Builder("datos_cardview.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build(),
                    true
                )
            }

            override fun onWrite(
                pages: Array<PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal,
                callback: WriteResultCallback
            ) {
                val attributes = printAttributes ?: run {
                    callback.onWriteFailed("PrintAttributes null")
                    return
                }

                val pdf = PrintedPdfDocument(context, attributes)
                val page = pdf.startPage(0)
                val canvas = page.canvas

                val pageWidth = page.info.pageWidth
                val pageHeight = page.info.pageHeight
                val margin = 50f

                // === COLORES CORPORATIVOS ===
                val primaryColor = ContextCompat.getColor(context, R.color.fons)
                val secondaryColor = Color.parseColor("#1565C0")
                val textDark = Color.parseColor("#263238")
                val textGray = Color.parseColor("#546E7A")

                // === HEADER CORPORATIVO ===
                val headerHeight = 120f
                val headerPaint = Paint().apply {
                    color = primaryColor
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), headerHeight, headerPaint)

                // Logo
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable._89_7)
                val logoWidth = 120
                val logoHeight = 80
                val scaledLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true)
                val logoY = (headerHeight - logoHeight) / 2f
                canvas.drawBitmap(scaledLogo, margin, logoY, null)

                // Título
                val titlePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 24f
                    isFakeBoldText = true
                }
                canvas.drawText("INFORME DE RENDIMENT", margin + 150f, 70f, titlePaint)

                val subtitlePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 14f
                }
                canvas.drawText("Resum académic de l'alumne", margin + 150f, 95f, subtitlePaint)

                // === CONTENIDO ===
                var y = 180f

                val sectionTitlePaint = Paint().apply {
                    textSize = 16f
                    isFakeBoldText = true
                    color = secondaryColor
                }

                val labelPaint = Paint().apply {
                    textSize = 14f
                    isFakeBoldText = true
                    color = textDark
                }

                val valuePaint = Paint().apply {
                    textSize = 14f
                    color = textGray
                }

                canvas.drawText("DETALLS DE LA PARTIDA", margin, y, sectionTitlePaint)
                y += 20

                val dividerPaint = Paint().apply {
                    strokeWidth = 2f
                    color = secondaryColor
                }
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
                y += 40

                fun drawRow(label: String, value: String) {
                    canvas.drawText(label, margin, y, labelPaint)
                    canvas.drawText(value, margin + 260f, y, valuePaint)
                    y += 32
                }

                drawRow("Tipus de partida:", tipus)
                drawRow("Respostes correctes:", correctes.toString())
                drawRow("Respostes incorrectes:", incorrectes.toString())
                drawRow("Experiencia obtinguda:", "$experiencia XP")

                // === MEDALLA SI CORRECTES == 10 ===
                if (correctes == 10) {
                    val medallaSize = 100f
                    val medallaX = pageWidth - margin - medallaSize
                    val medallaY = y

                    // Círculo dorado
                    val medallaPaint = Paint().apply {
                        style = Paint.Style.FILL
                        color = Color.parseColor("#FFD700")
                        isAntiAlias = true
                    }
                    canvas.drawCircle(
                        medallaX + medallaSize / 2,
                        medallaY + medallaSize / 2,
                        medallaSize / 2,
                        medallaPaint
                    )

                    // Borde de la medalla
                    val bordePaint = Paint().apply {
                        style = Paint.Style.STROKE
                        color = Color.parseColor("#FFA500")
                        strokeWidth = 6f
                        isAntiAlias = true
                    }
                    canvas.drawCircle(
                        medallaX + medallaSize / 2,
                        medallaY + medallaSize / 2,
                        medallaSize / 2,
                        bordePaint
                    )

                    // Estrella blanca
                    val starPaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val centerX = medallaX + medallaSize / 2
                    val centerY = medallaY + medallaSize / 2
                    val radius = medallaSize / 3

                    val path = Path()
                    for (i in 0 until 5) {
                        val angle = Math.toRadians((i * 72 - 90).toDouble())
                        val x = centerX + (radius * Math.cos(angle)).toFloat()
                        val yPos = centerY + (radius * Math.sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, yPos) else path.lineTo(x, yPos)

                        val innerAngle = Math.toRadians((i * 72 + 36 - 90).toDouble())
                        val innerRadius = radius / 2
                        val innerX = centerX + (innerRadius * Math.cos(innerAngle)).toFloat()
                        val innerY = centerY + (innerRadius * Math.sin(innerAngle)).toFloat()
                        path.lineTo(innerX, innerY)
                    }
                    path.close()
                    canvas.drawPath(path, starPaint)

                    // Texto felicitación
                    val felicitacionPaint = Paint().apply {
                        color = secondaryColor
                        textSize = 16f
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("¡Perfecte!", centerX, medallaY + medallaSize + 25f, felicitacionPaint)

                    y += medallaSize + 50f // ajustar y para contenido siguiente
                }

                // === FOOTER ===
                val footerPaint = Paint().apply {
                    textSize = 10f
                    color = Color.GRAY
                    textAlign = Paint.Align.CENTER
                }

                canvas.drawLine(margin, pageHeight - 70f, pageWidth - margin, pageHeight - 70f, dividerPaint)

                canvas.drawText(
                    "Document generat automáticament per MathEasy",
                    pageWidth / 2f,
                    pageHeight - 40f,
                    footerPaint
                )

                val path = Path().apply {
                    moveTo(pageWidth / 2f - 90f, pageHeight - 100f)
                    cubicTo(
                        pageWidth / 2f - 60f, pageHeight - 120f,
                        pageWidth / 2f + 20f, pageHeight - 80f,
                        pageWidth / 2f + 90f, pageHeight - 100f
                    )
                    cubicTo(
                        pageWidth / 2f + 60f, pageHeight - 90f,
                        pageWidth / 2f - 40f, pageHeight - 110f,
                        pageWidth / 2f - 90f, pageHeight - 100f
                    )
                }

                val signaturePaint = Paint().apply {
                    color = ContextCompat.getColor(context, R.color.fons)
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    isAntiAlias = true
                }
                canvas.drawPath(path, signaturePaint)

                val signatureText = "MathEasy®"
                val textPaint = Paint().apply {
                    color = ContextCompat.getColor(context, R.color.fons)
                    textSize = 16f
                    isFakeBoldText = true
                    style = Paint.Style.FILL
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(signatureText, pageWidth / 2f, pageHeight - 120f, textPaint)

                val signatureFooterPaint = Paint().apply {
                    color = ContextCompat.getColor(context, R.color.fons)
                    textSize = 11f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "Departament Académic MathEasy",
                    pageWidth / 2f,
                    pageHeight - 20f,
                    signatureFooterPaint
                )

                pdf.finishPage(page)
                pdf.writeTo(FileOutputStream(destination.fileDescriptor))
                pdf.close()

                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        }

        printManager.print("CardView Datos", printAdapter, null)
    }
}