package com.usaclean.sportreelzz.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.usaclean.sportreelzz.databinding.ItemStoryBinding

class HomeAdapter(private val itemClick: () -> Unit): RecyclerView.Adapter<HomeAdapter.ViewHolder>()  {

    inner class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            itemClick.invoke()
        }
    }
}