package com.example.matheasy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.R
import com.example.matheasy.databinding.InformeCardBinding
import com.example.matheasy.models.Informe

class InformeAdapter(var informes: List<Informe> = emptyList(), private val mContext: Context, private val onPrinterClicked: (Informe) -> Unit) : RecyclerView.Adapter<InformeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.informe_card, parent, false), mContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val informe = informes[position]
        holder.bind(informe)
        val binding = InformeCardBinding.bind(holder.itemView)
        binding.imageView.setOnClickListener { onPrinterClicked(informe) }
    }

    override fun getItemCount(): Int = informes.size

    class ViewHolder(val view: View, val mContext: Context) : RecyclerView.ViewHolder(view) {

        private val binding = InformeCardBinding.bind(view)

        fun bind(informe: Informe) {
            binding.nombreText.text = informe.Tipus_partida
            binding.ciutatText.text = informe.Respostes_correctes.toString()
            binding.comarcaText.text = informe.Respostes_incorrectes.toString()
            binding.cpText.text = informe.Experiencia.toString()
        }
    }
}