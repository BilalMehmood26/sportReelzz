package com.usaclean.sportreelzz.ui.main.topicDetails

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ItemDesignCommentLayoutBinding
import com.usaclean.sportreelzz.model.Reply
import com.usaclean.sportreelzz.ui.main.topicDetails.videoActivity.VideoViewActivity
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.util.regex.Pattern

class NestedReplyAdapter(
    val context: Context,
    val list: ArrayList<Reply>?,
    val commentId: String,
    val postId: String,
    val report: (String) -> Unit,
    val reply: (String) -> Unit
) : RecyclerView.Adapter<NestedReplyAdapter.ViewHolder>() {

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

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: NestedReplyAdapter.ViewHolder, position: Int) {
        var model = list!![position]

        FirebaseFirestore.getInstance().collection("Users")
            .document(model!!.fromId).get()
            .addOnSuccessListener {
                var fName = it.getString("userName")
                var imageUrl = it.getString("image")

                holder.binding.userNameTV.text = "$fName"
                if (imageUrl != null && imageUrl != "") {
                    Glide.with(context).load(imageUrl)
                        .error(R.drawable.place_holder)
                        .placeholder(R.drawable.place_holder)
                        .into(holder.binding.profileIV)
                }
            }



        holder.binding.apply {
            var thumbsUpCount = model.likedBy.filterValues { it }.keys.size
            thumbUpTV.text = thumbsUpCount.toString()
            thumbDownTV.text = model.likedBy.filterValues { !it }.keys.size.toString()
            contentTV.text = highlightMention(model.content)
            timeStampTv.text = convertMillisecondsToTimeFormat(model.videoTimeStamp)
            timeStampTv.paintFlags = timeStampTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            if (model.videoLink.isEmpty()) {
                videoIv.gone()
                playBtn.gone()
            } else {
                videoIv.visible()
                playBtn.visible()
                Glide.with(context).load(model.videoLink).into(videoIv)
            }

            if (model.type.equals("image")) {
                playBtn.gone()
            }

            videoIv.setOnClickListener {
                val intent = Intent(context, VideoViewActivity::class.java)
                intent.putExtra("URL", model.videoLink)
                intent.putExtra("type", model.type)
                context.startActivity(intent)
            }

            var currentUserLiked = model?.likedBy?.get(UserSession.user.id)

            if (currentUserLiked != null) {
                if (currentUserLiked) {
                    thumbUpIV.setImageResource(R.drawable.ic_like)
                    thumbDownIV.setImageResource(R.drawable.ic_dislike_black)
                } else {
                    thumbUpIV.setImageResource(R.drawable.ic_like)
                    thumbDownIV.setImageResource(R.drawable.ic_dislike_black)
                }
            } else {
                thumbUpIV.setImageResource(R.drawable.ic_like)
                thumbDownIV.setImageResource(R.drawable.ic_dislike_black)
            }

            val commentRef = FirebaseFirestore.getInstance().collection("Videos").document(postId)
                    .collection("Comments").document(commentId).collection("reply").document(model.commentId)

            if (model.fromId == UserSession.user.id) {
                deleteIV.visible()
                flagIV.gone()

                deleteIV.setOnClickListener {
                    Log.d("Logger", "CommentID: $commentId")
                    commentRef.delete()
                    list.remove(list[position])
                    notifyDataSetChanged()
                }
            } else {
                deleteIV.gone()
                flagIV.visible()
            }

            flagIV.setOnClickListener {
                report.invoke(model.commentId)
            }

            replyTv.setOnClickListener {
                reply.invoke(holder.binding.userNameTV.text.toString())
            }

            thumbUpIV.setOnClickListener {
                if (currentUserLiked != null) {
                    if (currentUserLiked!!) {
                        commentRef.update(
                            "likedBy.${UserSession.user.id}",
                            FieldValue.delete()
                        )
                        thumbsUpCount -= 1
                        currentUserLiked = false
                        thumbUpTV.text = thumbsUpCount.toString()
                    } else {
                        commentRef.update("likedBy.${UserSession.user.id}", true)
                        currentUserLiked = true
                        thumbsUpCount += 1
                        thumbUpTV.text = thumbsUpCount.toString()
                    }
                } else {
                    commentRef.update("likedBy.${UserSession.user.id}", true)
                    currentUserLiked = true
                    thumbsUpCount += 1
                    thumbUpTV.text = thumbsUpCount.toString()

                }
               // notifyItemChanged(position)
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

    private fun highlightMention(text: String): SpannableString {
        val spannable = SpannableString(text)

        val mentionPattern = "@\\w+"
        val matcher = Pattern.compile(mentionPattern).matcher(text)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            spannable.setSpan(
                BackgroundColorSpan(context.resources.getColor(R.color.highlight_blue )), // Set the desired color
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}