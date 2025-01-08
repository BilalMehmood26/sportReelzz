package com.usaclean.sportreelzz.ui.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.usaclean.sportreelzz.databinding.ItemDesignCommentLayoutBinding
import com.usaclean.sportreelzz.databinding.ItemRecursiveCommentBinding

class TestCommentAdapter(
    val context: Context,
    val list: java.util.ArrayList<TestComments>?,
    private val onReplyClicked: (TestComments) -> Unit
) : RecyclerView.Adapter<TestCommentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDesignCommentLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDesignCommentLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list!!.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = list?.get(position)

        holder.binding.apply {
            userNameTV.text = comment?.fromId
            contentTV.text = comment?.content

            // Set up replies
            if (comment?.reply!!.isNotEmpty()) {
                replyListRv.visibility = View.VISIBLE
                replyListRv.adapter = TestCommentAdapter(context,comment.reply, onReplyClicked)
                replyListRv.layoutManager = LinearLayoutManager(holder.itemView.context)
            } else {
                replyListRv.visibility = View.GONE
            }

            replyTv.setOnClickListener {
                onReplyClicked(comment)
            }
        }

    }

    private fun convertMillisecondsToTimeFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        return when {
            totalSeconds >= 3600 -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60

                String.format("%02d:%02d min", hours, minutes)
            }

            totalSeconds <= 59 -> {
                if (totalSeconds <= 9) {
                    String.format("00:0%d sec", totalSeconds)
                } else {
                    String.format("00:%02d sec", totalSeconds)
                }
            }

            else -> {
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60

                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}