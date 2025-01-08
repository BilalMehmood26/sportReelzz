package com.usaclean.sportreelzz.ui.main.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ItemStoryBinding
import com.usaclean.sportreelzz.model.Story
import com.usaclean.sportreelzz.model.User
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeAdapter(
    private val list: List<Story>,
    val context: Context,
    private val itemClick: (Story) -> Unit,
    private val editDetails: (Story) -> Unit,
    private val reportCLick: (Story) -> Unit,
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private var toggleBtn = 0

    inner class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Story) {
            binding.apply {
                Glide.with(context).load(item.videoLink).into(roundedImageView)
                durationTv.text = convertMillisecondsToTimeFormat(item.duration)
                titleTv.text = item.subject
                viewsTv.text = "${item.views} Views"

                if (item.userId == UserSession.user.id) {
                    menuIv.visible()
                    menuIv.setOnClickListener {
                        menuOption.apply {
                            visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                        deleteTv.setOnClickListener {
                            Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                            val commentRef = FirebaseFirestore.getInstance().collection("Videos")
                                .document(item.postId)
                            commentRef.delete()
                            notifyDataSetChanged()
                            menuOption.gone()
                        }

                        editTv.setOnClickListener {
                            editDetails.invoke(item)
                        }
                    }
                } else {
                    menuIv.gone()
                }

                db.collection("Users").document(item.userId!!).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.toObject(User::class.java)
                        userNameTv.text = user!!.userName
                        if (user.image!!.isNotEmpty()) {
                            Glide.with(context).load(user.image).into(profileIV)
                        } else {
                            profileIV.setImageResource(R.drawable.place_holder)
                        }
                    } else {
                        Log.d("LOGGER", "is Fail")
                        Toast.makeText(
                            context,
                            task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                when (item.language) {
                    "English" -> languageIv.setImageResource(R.drawable.ic_english)
                    "Dutch" -> languageIv.setImageResource(R.drawable.ic_dutch)
                    else -> languageIv.gone()
                }

                when(item.sports){
                    "Football" -> sportsIv.setImageResource(R.drawable.ic_football)
                    "Padel" -> sportsIv.setImageResource(R.drawable.ic_padel)
                    "Fitness" -> sportsIv.setImageResource(R.drawable.ic_fitness)
                    "Tennis" -> sportsIv.setImageResource(R.drawable.ic_tennis)
                    "Basketball" -> sportsIv.setImageResource(R.drawable.ic_basketball)
                    "Golf" -> sportsIv.setImageResource(R.drawable.ic_golf)
                    "Field Hockey" -> sportsIv.setImageResource(R.drawable.ic_hockey)
                    "Volleyball" -> sportsIv.setImageResource(R.drawable.ic_volleyball)
                    "Badminton" -> sportsIv.setImageResource(R.drawable.ic_badminton)
                    "Handball" -> sportsIv.setImageResource(R.drawable.ic_handball)
                    "Athletics" -> sportsIv.setImageResource(R.drawable.ic_athletics)
                    "Cycling" -> sportsIv.setImageResource(R.drawable.ic_cycling)
                    "Rugby" -> sportsIv.setImageResource(R.drawable.ic_rugby)
                    "Yoga/Pilates" -> sportsIv.setImageResource(R.drawable.ic_yoga_pilates)
                    "Skiing/Snowboarding" -> sportsIv.setImageResource(R.drawable.ic_snowboarding)
                }

                reportIv.setOnClickListener {
                    reportCLick.invoke(item)
                }
                root.setOnClickListener {
                    itemClick.invoke(item)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    private fun convertMillisecondsToTimeFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        return when {
            totalSeconds >= 3600 -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                String.format("%02d:%02d", hours, minutes)
            }

            totalSeconds <= 59 -> {
                if (totalSeconds <= 9) {
                    String.format("00:0%d", totalSeconds)
                } else {
                    String.format("00:%02d", totalSeconds)
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