package com.usaclean.sportreelzz.ui.main.topicDetails.reply

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ItemReplyBinding
import com.usaclean.sportreelzz.model.Comments

class CommentReplyAdapter(
    val context: Context,
    val list: List<Comments?>?
) : RecyclerView.Adapter<CommentReplyAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReplyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
            contentTV.text = model.content
        }
    }
}