package com.example.appreporte.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appreporte.databinding.ItemHijoPadreBinding

class HijosAdapter(
    private var hijos: List<Map<String, String>>,
    private val onHijoSelected: (Map<String, String>) -> Unit
) : RecyclerView.Adapter<HijosAdapter.HijoViewHolder>() {

    private var selectedPosition = -1

    class HijoViewHolder(val binding: ItemHijoPadreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HijoViewHolder {
        val binding = ItemHijoPadreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HijoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HijoViewHolder, position: Int) {
        val hijo = hijos[position]
        val isSelected = position == selectedPosition

        holder.binding.tvHijoName.text = hijo["names"]?.split(" ")?.firstOrNull() ?: "Hijo"
        
        // Highlight selection
        if (isSelected) {
            holder.binding.mcvHijo.strokeWidth = 4
            holder.binding.mcvHijo.strokeColor = holder.itemView.context.getColor(R.color.primary)
            holder.binding.tvHijoName.setTextColor(holder.itemView.context.getColor(R.color.primary))
            holder.binding.tvHijoName.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            holder.binding.mcvHijo.strokeWidth = 0
            holder.binding.tvHijoName.setTextColor(holder.itemView.context.getColor(R.color.on_surface_variant))
            holder.binding.tvHijoName.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onHijoSelected(hijo)
        }
    }

    override fun getItemCount() = hijos.size

    fun updateData(newList: List<Map<String, String>>) {
        hijos = newList
        selectedPosition = -1
        notifyDataSetChanged()
    }
}

