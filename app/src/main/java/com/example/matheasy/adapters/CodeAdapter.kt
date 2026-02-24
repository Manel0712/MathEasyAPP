package com.example.matheasy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matheasy.R
import com.example.matheasy.databinding.ItemCodeBinding

class CodeAdapter(private val codes: MutableList<String>, private val onCodeClicked: (code: String) -> Unit) :
    RecyclerView.Adapter<CodeAdapter.CodeViewHolder>() {

    inner class CodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvGameCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_code, parent, false)
        return CodeViewHolder(view)
    }

    override fun getItemCount(): Int = codes.size

    override fun onBindViewHolder(holder: CodeViewHolder, position: Int) {
        holder.tvCode.text = codes[position]
        val binding = ItemCodeBinding.bind(holder.itemView)
        binding.Code.setOnClickListener { onCodeClicked(codes[position]) }
    }
}