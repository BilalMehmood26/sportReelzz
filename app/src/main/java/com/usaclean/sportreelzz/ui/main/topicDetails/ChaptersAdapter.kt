package com.usaclean.sportreelzz.ui.main.topicDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.usaclean.sportreelzz.databinding.ItemChapterBinding
import com.usaclean.sportreelzz.model.Chapter
import com.usaclean.sportreelzz.model.Story

class ChaptersAdapter(
    private val list: List<Chapter>,
    val context: Context,
    private val itemClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChaptersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            chapterTv.text = item.chapterName
            timeStampTv.text = item.timeStamp

            root.setOnClickListener {
                itemClick.invoke(item)
            }
        }
    }
}