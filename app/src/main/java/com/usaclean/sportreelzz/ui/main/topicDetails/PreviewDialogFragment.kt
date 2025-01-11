package com.usaclean.sportreelzz.ui.main.topicDetails

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentPreviewDialogBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding


class PreviewDialogFragment(val videoUri: Uri, val cancel: () -> Unit) :
    DialogFragment() {

    private var _binding: FragmentPreviewDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentContext: Context

    private lateinit var mDialog: ProgressDialog
    var videoUrl = ""
    var type = ""

    private lateinit var player: ExoPlayer
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreviewDialogBinding.inflate(inflater)

       /* mDialog = ProgressDialog(fragmentContext)
        mDialog.apply {
            setMessage("Loading...")
            setCancelable(false)
            show()
        }*/

        player = ExoPlayer.Builder(fragmentContext).build()

        binding.apply {
            exoVideoView.player = player
            val mediaItem = MediaItem.fromUri(videoUri)
            player.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                       // mDialog.hide()
                    }
                }


                override fun onPlaybackStateChanged(state: Int) {
                }

                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(fragmentContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            cancelTv.setOnClickListener{
                cancel.invoke()
                dismiss()
            }

            doneTv.setOnClickListener {
               dismiss()
            }
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
       // mDialog.hide()
    }
}