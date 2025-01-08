package com.usaclean.sportreelzz

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.usaclean.sportreelzz.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    lateinit var binding: ActivityVideoBinding
    lateinit var mDialog: ProgressDialog
    var videoUrl = ""
    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDialog = ProgressDialog(this)
        mDialog.setMessage("Loading...")
        mDialog.setCancelable(false)


        videoUrl = intent.getStringExtra("URL").toString()

        player = ExoPlayer.Builder(this).build()

        val uri: Uri = Uri.parse(videoUrl)

        binding.apply {

            backIV.setOnClickListener {
                finish()
            }

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
                    } else {
                        mDialog.show()
                    }
                }


                override fun onPlaybackStateChanged(state: Int) {

                }

                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(this@VideoActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
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