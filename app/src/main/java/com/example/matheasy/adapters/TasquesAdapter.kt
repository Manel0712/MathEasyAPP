package com.example.matheasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.databinding.InformeCardBinding
import com.example.matheasy.databinding.ItemTascaBinding
import com.example.matheasy.models.Informe
import com.example.matheasy.models.Tasca

class TasquesAdapter(private val tasques: List<Tasca>, private val onTascaClicked: ((Tasca) -> Unit)?) :
    RecyclerView.Adapter<TasquesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTascaBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemTascaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = tasques.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tasca = tasques[position]

        holder.binding.nombreText.text = tasca.Nom
        holder.binding.ciutatText.text = tasca.Data_obertura.toString()
        holder.binding.comarcaText.text = tasca.Data_tancament.toString()
        holder.binding.cpText.text = tasca.pivot.Estat_tramesa

        holder.binding.tasca.setOnClickListener { onTascaClicked?.invoke(tasca) }
    }
}