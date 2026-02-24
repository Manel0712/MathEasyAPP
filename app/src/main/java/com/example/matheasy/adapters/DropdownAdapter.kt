package com.example.matheasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.databinding.ItemOptionBinding

class DropdownAdapter(
    private val options: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DropdownAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]

        holder.binding.tvOption.text = option

        holder.binding.root.setOnClickListener {
            onClick(option)
        }
    }
}