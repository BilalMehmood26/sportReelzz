package com.usaclean.sportreelzz.ui.main.myTopic.fullscreen

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityFullScreenVideoBinding
import com.usaclean.sportreelzz.databinding.ActivityMainBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible

class FullScreenVideoActivity : AppCompatActivity() {

    private var _binding: ActivityFullScreenVideoBinding? = null
    private val binding get() = _binding!!
    private lateinit var runnable: Runnable

    private var videoUrl = ""
    private var videoTimeStamp: Long = 0

    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        _binding = ActivityFullScreenVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoUrl = intent.getStringExtra("url").toString()
        videoTimeStamp = intent.getLongExtra("timeStamp", 0)

        player = ExoPlayer.Builder(this).build()
        binding.apply {

            fullScreen.setOnClickListener {
                finish()
            }

            exoVideoView.player = player
            val uri: Uri = Uri.parse(videoUrl)
            val mediaItem = MediaItem.fromUri(uri)

            player.apply {
                setMediaItem(mediaItem)
                prepare()
                player.seekTo(videoTimeStamp)
                playVideo()
                checkVisibility()
            }

            playIv.setOnClickListener {
                if (player.isPlaying) {
                    pauseVideo()
                } else {
                    playVideo()
                }
            }

            forwardIv.setOnClickListener {
                val forward = player.currentPosition + 5000
                player.seekTo(forward)
            }

            backwardIv.setOnClickListener {
                val backward = player.currentPosition - 5000
                player.seekTo(backward)
            }

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    binding.progressBar.gone()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    binding.progressBar.gone()
                }

                override fun onPlayerError(error: PlaybackException) {
                    binding.progressBar.gone()
                    Toast.makeText(this@FullScreenVideoActivity, error.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }

    @OptIn(UnstableApi::class)
    private fun checkVisibility() {
        runnable = Runnable {
            binding.apply {
                if (exoVideoView.isControllerFullyVisible) {
                    constraintLayout2.visible()
                    playIv.visible()
                    forwardIv.visible()
                    backwardIv.visible()
                } else {
                    constraintLayout2.gone()
                    playIv.gone()
                    forwardIv.gone()
                    backwardIv.gone()
                }
                Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun playVideo() {
        binding.apply {
            playIv.setImageResource(R.drawable.ic_pause)
            player.play()
        }
    }

    private fun pauseVideo() {
        binding.apply {
            playIv.setImageResource(R.drawable.ic_play)
            player.pause()
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}