package com.usaclean.sportreelzz.ui.main.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ItemFilterBinding
import com.usaclean.sportreelzz.model.Filter

class FilterAdapter(
    val context: Context,
    val list: ArrayList<Filter>,
    val onCLick: (List<Filter>) -> Unit
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    private val filteredList:ArrayList<Filter> = arrayListOf()
    inner class ViewHolder(val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        Glide.with(context).load(model.image)
            .error(R.drawable.place_holder)
            .placeholder(R.drawable.place_holder)
            .into(holder.binding.imageIv)

        holder.binding.titleTv.text = model.title

        if (list[position].isSelected) {
            holder.binding.titleTv.setBackgroundResource(R.drawable.edit_text_selected)
            filteredList.add(list[position])
        }

        holder.binding.root.setOnClickListener {
            if (list[position].isSelected) {
                list[position].isSelected = false
                holder.binding.titleTv.setBackgroundResource(R.drawable.edit_text_grey_12)
                filteredList.remove(model)
                onCLick.invoke(filteredList)
            } else {
                list[position].isSelected = true
                holder.binding.titleTv.setBackgroundResource(R.drawable.edit_text_selected)
                filteredList.add(model)
                onCLick.invoke(filteredList)
            }
        }
    }
}