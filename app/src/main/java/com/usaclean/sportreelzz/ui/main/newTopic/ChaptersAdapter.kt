package com.usaclean.sportreelzz.ui.main.newTopic

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.usaclean.sportreelzz.databinding.ItemVideoChapterBinding
import com.usaclean.sportreelzz.model.Chapter

class ChaptersAdapter(
    val context: Context,
    private val list: ArrayList<Chapter>,
    private val closeClick: (Int) -> Unit
) :
    RecyclerView.Adapter<ChaptersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemVideoChapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemVideoChapterBinding.inflate(
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
        val item = list[position]

        holder.binding.apply {

            //chapterEt.setText(item.chapterName)
            //timeStampEt.setText(item.timeStamp)

            Log.d("Logger", "onBindViewHolder: ${item}")

            deleteBtn.setOnClickListener {
                closeClick.invoke(position)
            }

            chapterEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.chapterName = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

            timeStampEt.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.timeStamp = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return

                    val text = s.toString()
                    if (text.length == 3 && !text.contains(":")) { // Add colon after 2 characters
                        isUpdating = true
                        val formatted = text.substring(0, 2) + ":" + text.substring(2)
                        timeStampEt.setText(formatted)
                        timeStampEt.setSelection(formatted.length) // Move cursor to the end
                        isUpdating = false
                    }
                }
            })
        }
    }

    fun updateList(): List<Chapter>{
        return list
    }
}