package com.usaclean.sportreelzz.ui.main.topicDetails.videoActivity

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityVideoViewBinding
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible

class VideoViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityVideoViewBinding
    lateinit var mDialog: ProgressDialog
    var videoUrl = ""
    var type = ""
    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDialog = ProgressDialog(this)
        mDialog.setMessage("Loading...")
        mDialog.setCancelable(false)
        mDialog.show()

        videoUrl = intent.getStringExtra("URL")!!
        type = intent.getStringExtra("type")!!

        player = ExoPlayer.Builder(this).build()

        val uri: Uri = Uri.parse(videoUrl)

        binding.apply {

            if (type == "image") {
                myZoomageView.visible()
                exoVideoView.gone()
                mDialog.hide()
                Glide.with(this@VideoViewActivity).load(uri).into(myZoomageView)
            } else {
                exoVideoView.player = player
                val mediaItem = MediaItem.fromUri(uri)
                player.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
                player.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            mDialog.hide()
                        }
                    }


                    override fun onPlaybackStateChanged(state: Int) {
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(this@VideoViewActivity, error.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }

            backIV.setOnClickListener {
                finish()
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}