package com.example.matheasy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.databinding.ItemTemaBinding
import com.example.matheasy.models.Tema

class TemesAdapter(
    private val temes: MutableList<Tema>
) : RecyclerView.Adapter<TemesAdapter.ViewHolder>() {

    private var expandedPosition = -1

    class ViewHolder(val binding: ItemTemaBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemTemaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = temes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tema = temes[position]

        holder.binding.txtTema.text = tema.Nom

        holder.binding.recyclerTasques.adapter =
            TasquesAdapter(tema.tasques, null)

        holder.binding.recyclerTasques.layoutManager =
            LinearLayoutManager(holder.itemView.context)

        val isExpanded = position == expandedPosition

        holder.binding.recyclerTasques.visibility =
            if (isExpanded) View.VISIBLE else View.GONE

        holder.binding.txtTema.setOnClickListener {

            expandedPosition =
                if (isExpanded) -1 else position

            notifyDataSetChanged()
        }
    }
}