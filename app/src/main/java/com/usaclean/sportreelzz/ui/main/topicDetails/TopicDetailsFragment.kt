package com.usaclean.sportreelzz.ui.main.topicDetails

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentFilterBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding
import com.usaclean.sportreelzz.databinding.LayoutReplyBinding
import com.usaclean.sportreelzz.databinding.LayoutReportBinding
import com.usaclean.sportreelzz.model.Chapter
import com.usaclean.sportreelzz.model.Comment
import com.usaclean.sportreelzz.model.Comments
import com.usaclean.sportreelzz.model.Reply
import com.usaclean.sportreelzz.model.Story
import com.usaclean.sportreelzz.model.User
import com.usaclean.sportreelzz.ui.main.myTopic.fullscreen.FullScreenVideoActivity
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.util.UUID
import java.util.regex.Pattern


class TopicDetailsFragment : Fragment() {

    companion object {

        private const val REQUEST_CODE_SELECT_IMAGE_VIDEO = 1001
        private const val REQUEST_CODE_PERMISSIONS = 1002
        private const val REQUEST_CODE_CAPTURE_IMAGE = 1003
        private const val REQUEST_CODE_RECORD_VIDEO = 1004
        fun newInstance() = TopicDetailsFragment()
    }

    private var _binding: FragmentTopicDetailsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "Detail_Activity_TAG"
    private var videoURI: Uri? = null
    private lateinit var dialog: Dialog

    private lateinit var previewDialogFragment: PreviewDialogFragment
    private lateinit var adapter: CommentAdapter
    private var commentsList: ArrayList<Comments?>? = ArrayList()
    private var commentList: ArrayList<Comment?>? = ArrayList()

    private val db = Firebase.firestore
    private lateinit var fragmentContext: Context
    private val storyList: ArrayList<Story> = ArrayList()

    private lateinit var player: ExoPlayer
    private lateinit var runnable: Runnable

    private val requiredPermissions: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private val args: TopicDetailsFragmentArgs by navArgs()

    private var chapterList: ArrayList<Chapter> = ArrayList()
    private lateinit var chaptersAdapter: ChaptersAdapter

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopicDetailsBinding.inflate(inflater)

        player = ExoPlayer.Builder(fragmentContext).setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000).build()
        dialog = Dialog(fragmentContext)
        binding.apply {

            getUserDetails()
            getVideoList()
            //getCommentData()
            getNestedCommentData()
            getLikesCount()
            updateViews()

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

            Glide.with(fragmentContext).load(args.story.videoLink).into(thumbnailIv)
            titleTv.text = args.story.subject
            questionTv.text = "${args.story.question}?"
            val uri: Uri = Uri.parse(args.story.videoLink)

            viewsTv.text = "${args.story.views} Views"
            exoVideoView.player = player


            player.apply {
                init(uri)
                checkVisibility()
            }

            playIv.setOnClickListener {
                if (player.isPlaying) {
                    pauseVideo()
                } else {
                    playVideo()
                }
            }


            fullScreen.setOnClickListener {
                val intent = Intent(fragmentContext, FullScreenVideoActivity::class.java)
                intent.putExtra("timeStamp", player.currentPosition)
                intent.putExtra("url", args.story.videoLink)
                startActivity(intent)
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
                    Toast.makeText(fragmentContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })

            shareIv.setOnClickListener {
                shareFile(args.story.videoLink)
            }

            var currentUserLiked = args.story.likedBy?.get(UserSession.user.id)
            Log.d(TAG, "setListener: $currentUserLiked")

            val postsRef =
                FirebaseFirestore.getInstance().collection("Videos").document(args.story.postId)

            /*   thumbUpIv.setOnClickListener {
                   if (currentUserLiked != null) {
                       if (currentUserLiked!!) {
                           postsRef.update(
                               "likedBy.${UserSession.user.id}",
                               FieldValue.delete()
                           )
                           getLikesCount()
                           currentUserLiked = false
                       } else {
                           postsRef.update("likedBy.${UserSession.user.id}", true)
                           getLikesCount()
                           currentUserLiked = true
                       }
                   } else {
                       postsRef.update("likedBy.${UserSession.user.id}", true)
                       getLikesCount()
                       currentUserLiked = true
                   }
               }

               thumbDownIv.setOnClickListener {
                   if (currentUserLiked != null) {
                       if (!currentUserLiked!!) {
                           postsRef.update(
                               "likedBy.${UserSession.user.id}",
                               FieldValue.delete()
                           )
                           getLikesCount()
                           currentUserLiked = true
                       } else {
                           postsRef.update("likedBy.${UserSession.user.id}", false)
                               .addOnSuccessListener {
                                   getLikesCount()
                                   currentUserLiked = false
                               }
                               .addOnFailureListener { e ->
                                   Log.e("dislike", "Error adding dislike: ${e.message}", e)
                               }
                       }
                   } else {
                       postsRef.update("likedBy.${UserSession.user.id}", false)
                           .addOnSuccessListener {
                               getLikesCount()
                               currentUserLiked = false
                           }
                           .addOnFailureListener { e ->
                               Log.e("dislike", "Error adding dislike: ${e.message}", e)
                           }
                   }
               }*/

            val chapterList = args.story.chapters.map { (key, value) ->
                Chapter(chapterName = key, timeStamp = value)
            }

            /*
                        chaterRv.apply {
                            chaptersAdapter = ChaptersAdapter(chapterList, fragmentContext) {
                                player.seekTo(timeToMilliseconds(it.timeStamp))
                            }
                            adapter = chaptersAdapter
                            layoutManager = LinearLayoutManager(
                                requireActivity(),
                                LinearLayoutManager.VERTICAL, false
                            )
                        }*/

            /*    binding.sendIv.setOnClickListener {
                    var commentText = binding.commentEt.text.toString()

                    if (commentText.isEmpty()) {
                        Toast.makeText(fragmentContext, "Field Empty", Toast.LENGTH_SHORT).show()
                    } else {
                        progressBar.visible()
                        if (videoURI.toString().contains(".mp4")) {
                            val videoPathReference =
                                FirebaseStorage.getInstance().reference.child("Posts/Comment/Video/${args.story.postId}.mp4")
                            val uploadVideoToStorage = videoPathReference.putFile(videoURI!!)
                            uploadVideoToStorage.addOnSuccessListener {
                                videoPathReference.downloadUrl.addOnSuccessListener { videoUri ->
                                    var commentId = UUID.randomUUID().toString()
                                    val commentMap = hashMapOf(
                                        "commentId" to commentId,
                                        "content" to commentText,
                                        "timeStamp" to System.currentTimeMillis(),
                                        "videoTimeStamp" to player.currentPosition,
                                        "fromId" to UserSession.user.id,
                                        "videoLink" to videoUri,
                                        "toId" to args.story.userId,
                                        "type" to "video"
                                    )

                                    FirebaseFirestore.getInstance().collection("Videos")
                                        .document(args.story.postId).collection("Comments")
                                        .document(commentId).set(commentMap)
                                        .addOnSuccessListener {
                                            FirebaseFirestore.getInstance().collection("Videos")
                                                .document(args.story.postId)
                                                .update("commentUsers.${UserSession.user.id}", true)
                                            progressBar.gone()
                                            videoURI = null
                                            binding.commentEt.setText("");

                                        }.addOnFailureListener {
                                            progressBar.gone()
                                            Toast.makeText(
                                                fragmentContext,
                                                "Error: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        } else if (videoURI.toString().contains(".jpg")) {
                            val videoPathReference =
                                FirebaseStorage.getInstance().reference.child("Posts/Comment/Image/${args.story.postId}.jpg")
                            val uploadVideoToStorage = videoPathReference.putFile(videoURI!!)
                            uploadVideoToStorage.addOnSuccessListener {
                                videoPathReference.downloadUrl.addOnSuccessListener { videoUri ->
                                    var commentId = UUID.randomUUID().toString()
                                    val commentMap = hashMapOf(
                                        "commentId" to commentId,
                                        "content" to commentText,
                                        "timeStamp" to System.currentTimeMillis(),
                                        "videoTimeStamp" to player.currentPosition,
                                        "fromId" to UserSession.user.id,
                                        "videoLink" to videoUri,
                                        "toId" to args.story.userId,
                                        "type" to "image"
                                    )

                                    FirebaseFirestore.getInstance().collection("Videos")
                                        .document(args.story.postId).collection("Comments")
                                        .document(commentId).set(commentMap)
                                        .addOnSuccessListener {
                                            FirebaseFirestore.getInstance().collection("Videos")
                                                .document(args.story.postId)
                                                .update("commentUsers.${UserSession.user.id}", true)
                                            progressBar.gone()
                                            videoURI = null
                                            binding.commentEt.setText("");

                                        }.addOnFailureListener {
                                            progressBar.gone()
                                            Toast.makeText(
                                                fragmentContext,
                                                "Error: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        } else {
                            var commentId = UUID.randomUUID().toString()
                            val commentMap = hashMapOf(
                                "commentId" to commentId,
                                "content" to commentText,
                                "timeStamp" to System.currentTimeMillis(),
                                "videoTimeStamp" to player.currentPosition,
                                "fromId" to UserSession.user.id,
                                "toId" to args.story.userId,
                                "type" to "text"
                            )

                            FirebaseFirestore.getInstance().collection("Videos")
                                .document(args.story.postId).collection("Comments")
                                .document(commentId).set(commentMap)
                                .addOnSuccessListener {
                                    FirebaseFirestore.getInstance().collection("Videos")
                                        .document(args.story.postId)
                                        .update("commentUsers.${UserSession.user.id}", true)
                                    progressBar.gone()
                                    videoURI = null
                                    binding.commentEt.setText("");

                                }.addOnFailureListener {
                                    progressBar.gone()
                                    Toast.makeText(
                                        fragmentContext,
                                        "Error: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }*/

            sendIv.setOnClickListener {

                val commentText = commentEt.text.toString()
                progressBar.visible()
                when {
                    videoURI.toString().contains(".mp4") -> {
                        uploadCommentWithMedia(
                            "Posts/Comment/Video/${args.story.postId}.mp4",
                            "video",
                            commentText
                        )
                    }

                    videoURI.toString().contains(".jpg") -> {
                        uploadCommentWithMedia(
                            "Posts/Comment/Image/${args.story.postId}.jpg",
                            "image",
                            commentText
                        )
                    }

                    else -> {
                        uploadCommentText(commentText)
                    }
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

            previewBtn.setOnClickListener {
                previewDialogFragment = PreviewDialogFragment(videoURI!!)
                previewDialogFragment.show(childFragmentManager, "")
            }

            attachmentIv.setOnClickListener {
                if (hasRequiredPermissions()) {
                    showImageVideoDialog()
                } else {
                    requestPermissions()
                }
            }
        }
        return binding.root
    }

    private fun init(videoURI: Uri) {
        player.apply {
            val mediaItem = MediaItem.fromUri(videoURI)
            setMediaItem(mediaItem)
            prepare()
            playVideo()
        }
    }

    private fun uploadCommentWithMedia(storagePath: String, type: String, content: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
        val uploadTask = storageRef.putFile(videoURI!!)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val commentMap = createCommentMap(content, type, uri.toString())
                saveCommentToFirestore(commentMap)
            }.addOnFailureListener {
                handleError(it.message)
                Log.d(TAG, "uploadCommentWithMedia: ${it.message}")
            }
        }.addOnFailureListener {
            handleError(it.message)
            Log.d(TAG, "uploadCommentWithMedia Failure: ${it.message}")
        }
    }

    private fun uploadCommentText(content: String) {
        val commentMap = createCommentMap(content, "text", null)
        saveCommentToFirestore(commentMap)
    }

    private fun createCommentMap(
        content: String,
        type: String,
        mediaLink: String?
    ): Map<String, Any> {
        return hashMapOf(
            "commentId" to UUID.randomUUID().toString(),
            "content" to content,
            "timeStamp" to System.currentTimeMillis(),
            "videoTimeStamp" to player.currentPosition,
            "fromId" to UserSession.user.id,
            "toId" to args.story.userId,
            "type" to type
        ).apply {
            mediaLink?.let { this["videoLink"] = it }
        }
    }

    private fun saveCommentToFirestore(commentMap: Map<String, Any>) {
        val postId = args.story.postId
        val commentId = commentMap["commentId"] as String

        FirebaseFirestore.getInstance().collection("Videos")
            .document(postId).collection("Comments")
            .document(commentId).set(commentMap)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("Videos")
                    .document(postId)
                    .update("commentUsers.${UserSession.user.id}", true)

                binding.progressBar.gone()
                videoURI = null
                binding.previewBtn.gone()
                binding.commentEt.setText("")
            }
            .addOnFailureListener {
                handleError(it.message)

            }
    }

    private fun handleError(message: String?) {
        binding.progressBar.gone()
        Toast.makeText(fragmentContext, "Error: $message", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "handleError: $message")
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

    private fun hasRequiredPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    fragmentContext,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun showImageVideoDialog() {
        val options = arrayOf<CharSequence>(
            "Choose Video from Gallery",
            "Choose Picture from Gallery",
            "Record Video"
        )

        val builder = AlertDialog.Builder(fragmentContext)
        builder.setTitle("Select Option")
        builder.setItems(options) { _, item ->
            when (item) {
                0 -> openGalleryOrFilePicker()
                1 -> openImageOrFilePicker()
                2 -> recordVideo()
            }
        }

        builder.show()
    }

    private fun openImageOrFilePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQUEST_CODE_CAPTURE_IMAGE)
    }

    private fun recordVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO)
    }

    private fun openGalleryOrFilePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_VIDEO)
    }

    private fun requestPermissions() {
        requestPermissions(requiredPermissions, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageVideoDialog()
            } else {
                // Handle permission denied case here
            }
        }
    }

    private fun shareFile(file: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_TEXT, file)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share File via"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE_VIDEO && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data
            if (selectedMediaUri != null) {
                videoURI = selectedMediaUri
                Log.d("LOgger", "onActivityResult: $videoURI")
                if(dialog!=null) dialog.findViewById<TextView>(R.id.preview_btn).visible()
                binding.previewBtn.visible()
                Toast.makeText(
                    fragmentContext,
                    "Video has been uploaded successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == REQUEST_CODE_RECORD_VIDEO && resultCode == Activity.RESULT_OK) {
            val capturedVideoUri = data?.data
            if (capturedVideoUri != null) {
                videoURI = capturedVideoUri
                dialog.findViewById<TextView>(R.id.preview_btn).visible()
                binding.previewBtn.visible()
                Log.d("LOgger", "onActivityResult: $videoURI")
                Toast.makeText(
                    fragmentContext,
                    "Video has been uploaded successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data
            if (selectedMediaUri != null) {
                binding.previewBtn.visible()
                dialog.findViewById<TextView>(R.id.preview_btn).visible()
                videoURI = selectedMediaUri
                Log.d("LOgger", "onActivityResult: $videoURI")
                Toast.makeText(
                    fragmentContext,
                    "Video has been uploaded successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateViews() {
        val updateViews = args.story.views + 1
        db.collection("Videos").document(args.story.postId).update("views", updateViews)
    }

    private fun setAdapter() {
        binding.apply {
            val layoutManager =
                LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
            layoutManager.stackFromEnd = true
            commentRv.layoutManager = layoutManager
            val reversedCommentsList = commentList!!.reversed().toCollection(ArrayList())
            adapter = CommentAdapter(
                fragmentContext,
                reversedCommentsList,
                args.story.postId,
                playBack = {
                    player.seekTo(it)
                },
                report = {
                    reportDialog(args.story, it)
                },
                reply = { comment, userName ->
                    /*val action =
                        TopicDetailsFragmentDirections.actionTopicDetailsFragmentToReplyCommentFragment(
                            it,
                            args.story.postId
                        )
                    findNavController().navigate(action)*/
                    showReplyInput(comment, userName)
                })
            commentRv.adapter = adapter
        }
    }

    private fun showReplyInput(parentComment: Comment, userName: String) {
        dialog.setCancelable(true)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = LayoutReplyBinding.inflate(LayoutInflater.from(fragmentContext))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        dialogBinding.apply {
            cancelTv.setOnClickListener {
                dialog.dismiss()
            }

            replyEt.apply {
                hint = "Write a reply..."
                if (userName.isNotEmpty()) setText("@$userName ")
                requestFocus()
            }

            highlightMention(replyEt)

            attachmentIv.setOnClickListener {
                if (hasRequiredPermissions()) {
                    showImageVideoDialog()
                } else {
                    requestPermissions()
                }
            }


            previewBtn.setOnClickListener {
                previewDialogFragment = PreviewDialogFragment(videoURI!!)
                previewDialogFragment.show(childFragmentManager, "")
            }
            replyTv.setOnClickListener {
                if (replyEt.text.isNotEmpty()) {
                    when {
                        videoURI.toString().contains(".mp4") -> {
                            uploadNestedCommentWithMedia(
                                "Posts/Comment/Video/${args.story.postId}.mp4",
                                "video",
                                replyEt.text.toString(),
                                parentComment.commentId
                            )
                        }

                        videoURI.toString().contains(".jpg") -> {
                            uploadNestedCommentWithMedia(
                                "Posts/Comment/Image/${args.story.postId}.jpg",
                                "image",
                                replyEt.text.toString(),
                                parentComment.commentId
                            )
                        }

                        else -> {
                            replyUploadCommentText(replyEt.text.toString(), parentComment.commentId)
                        }
                    }
                } else {
                    replyEt.error = "Required"
                }

                /*  if (replyEt.text.isNotEmpty()) {
                      val replyID = UUID.randomUUID().toString()
                      val replyModel = Comments(
                          replyID,
                          replyEt.text.toString(),
                          UserSession.user.id,
                          System.currentTimeMillis(),
                          player.currentPosition,
                          args.story.userId
                      )
                      db.collection("Videos").document(args.story.postId).collection("Comments")
                          .document(parentComment.commentId).collection("reply").document(replyID)
                          .set(replyModel)
                      adapter.notifyDataSetChanged()
                  }*/
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun replyUploadCommentText(content: String, commentId: String) {
        val commentMap = createNestedCommentMap(content, commentId, "text", null)
        saveNestedCommentToFireStore(commentMap, commentId)
    }

    private fun saveNestedCommentToFireStore(
        commentMap: Map<String, Any>,
        parentCommentID: String
    ) {
        val postId = args.story.postId
        val commentId = commentMap["commentId"] as String

        FirebaseFirestore.getInstance().collection("Videos")
            .document(postId).collection("Comments").document(parentCommentID).collection("reply")
            .document(commentId).set(commentMap)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("Videos")
                    .document(postId)
                    .update("commentUsers.${UserSession.user.id}", true)
                getNestedCommentData()

                binding.progressBar.gone()
                videoURI = null
                binding.previewBtn.gone()
                binding.commentEt.setText("")
            }
            .addOnFailureListener {
                handleError(it.message)

            }
    }

    private fun createNestedCommentMap(
        content: String,
        commentId: String,
        type: String,
        mediaLink: String?
    ): Map<String, Any> {
        return hashMapOf(
            "commentId" to UUID.randomUUID().toString(),
            "content" to content,
            "timeStamp" to System.currentTimeMillis(),
            "videoTimeStamp" to player.currentPosition,
            "fromId" to UserSession.user.id,
            "toId" to args.story.userId,
            "type" to type
        ).apply {
            mediaLink?.let { this["videoLink"] = it }
        }
    }

    private fun uploadNestedCommentWithMedia(
        storagePath: String,
        type: String,
        content: String,
        parentCommentID: String
    ) {
        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
        val uploadTask = storageRef.putFile(videoURI!!)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val commentMap =
                    createNestedCommentMap(content, parentCommentID, type, uri.toString())
                saveNestedCommentToFireStore(commentMap, parentCommentID)
            }.addOnFailureListener {
                handleError(it.message)
                Log.d(TAG, "uploadCommentWithMedia: ${it.message}")
            }
        }.addOnFailureListener {
            handleError(it.message)
            Log.d(TAG, "uploadCommentWithMedia Failure: ${it.message}")
        }
    }

    private fun highlightMention(editText: EditText) {
        val text = editText.text.toString()
        val spannable = SpannableString(text)

        // Define the pattern for mentions (e.g., "@username")
        val mentionPattern = "@\\w+"
        val matcher = Pattern.compile(mentionPattern).matcher(text)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            // Apply color span to the mention
            spannable.setSpan(
                BackgroundColorSpan(resources.getColor(R.color.highlight_blue)), // Set the desired color
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                BackgroundColorSpan(resources.getColor(R.color.highlight_blue)), // Set the desired color
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        editText.setText(spannable)
        editText.setSelection(editText.text.length) // Keep the cursor at the end
    }

    private fun reportDialog(videoDetails: Story, commentID: String) {
        val dialog = Dialog(fragmentContext)
        var selectedText = ""

        dialog.apply {

            val dialogBinding = LayoutReportBinding.inflate(LayoutInflater.from(fragmentContext))
            setCancelable(true)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(dialogBinding.root)
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setGravity(Gravity.CENTER)
            }


            dialogBinding.apply {
                cancelBtn.setOnClickListener {
                    dialog.hide()
                }

                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
                    selectedText = selectedRadioButton?.text.toString()
                }

                reportTv.setOnClickListener {
                    reportVideo(videoDetails.userId, videoDetails.postId, selectedText, commentID)
                    dialog.hide()
                }
            }

            dialog.show()
        }
    }

    private fun reportVideo(userID: String, postID: String, reason: String, commentID: String) {
        binding.progressBar.visible()
        val reportID = UUID.randomUUID().toString()
        val reportVideo = hashMapOf(
            "postID" to postID,
            "reportID" to reportID,
            "commentId" to commentID,
            "userID" to userID,
            "type" to "comment",
            "reason" to reason
        )

        db.collection("Report").document(reportID).set(reportVideo).addOnSuccessListener {
            binding.progressBar.gone()
            Toast.makeText(fragmentContext, "Reported", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBar.gone()
            Toast.makeText(fragmentContext, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNestedCommentData() {
        commentList!!.clear()
        binding.progressBar.visible()
        val commentsCollection = FirebaseFirestore.getInstance()
            .collection("Videos")
            .document(args.story.postId)
            .collection("Comments")
            .orderBy("timeStamp", Query.Direction.DESCENDING)

        commentsCollection.addSnapshotListener { queryDocumentSnapshots, error ->
            if (error != null) {
                Log.d("LOGGER", "Exception: ${error.message}")
                binding.progressBar.gone()
                return@addSnapshotListener
            }

            commentList!!.clear()
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                binding.progressBar.gone()
                val tasks = mutableListOf<Task<QuerySnapshot>>()

                for (documentSnapshot in queryDocumentSnapshots) {
                    val model = documentSnapshot.toObject(Comment::class.java).apply {
                        commentId = documentSnapshot.id
                    }

                    val repliesTask = FirebaseFirestore.getInstance()
                        .collection("Videos")
                        .document(args.story.postId)
                        .collection("Comments").document(model.commentId)
                        .collection("reply")
                        .get()
                        .addOnSuccessListener { repliesSnapshot ->
                            val repliesList = repliesSnapshot.map { replyDoc ->
                                replyDoc.toObject(Reply::class.java).apply {
                                    commentId = replyDoc.id
                                }
                            }.reversed().toCollection(ArrayList())
                            model.reply = repliesList
                        }
                    tasks.add(repliesTask)
                    commentList!!.add(model)
                }

                Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnSuccessListener {
                    binding.progressBar.gone()
                    setAdapter()
                }.addOnFailureListener { e ->
                    Log.d("LOGGER", "Error fetching replies: ${e.message}")
                    binding.progressBar.gone()
                }
            } else {
                binding.progressBar.gone()
            }
        }
    }

    private fun getCommentData() {
        commentsList!!.clear()
        binding.progressBar.visible()
        FirebaseFirestore.getInstance().collection("Videos")
            .document(args.story.postId).collection("Comments")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { queryDocumentSnapshots, error ->
                if (error != null) {
                    Log.d("LOGGER", "Exception " + error.message);
                    binding.progressBar.gone()
                    return@addSnapshotListener
                }
                commentsList!!.clear()
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                    binding.progressBar.gone()
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val model = documentSnapshot.toObject(Comments::class.java)
                        commentsList!!.add(model)
                        Log.d("LOGGER", "getCommentData: ${commentsList!!.size}")
                    }
                    binding.progressBar.visible()
                    setAdapter()
                }

            }

    }

    private fun getVideoList() {
        binding.progressBar.visible()
        db.collection("Videos").addSnapshotListener { value, error ->

            if (error != null) {
                Toast.makeText(fragmentContext, "", Toast.LENGTH_SHORT).show()
                binding.progressBar.gone()
                return@addSnapshotListener
            }

            binding.progressBar.gone()
            storyList.clear()
            value!!.forEach {
                val video = it.toObject(Story::class.java)
                if (video.userId.equals(args.story.userId)) {
                    storyList.add(video)
                }
            }
            binding.videosSizeTv.text = "${storyList.size} Videos"
        }
    }

    private fun getLikesCount() {
        FirebaseFirestore.getInstance().collection("Videos")
            .document(args.story.postId)
            .addSnapshotListener { queryDocumentSnapshots, error ->
                if (error != null) {
                    Log.d("LOGGER", "Exception " + error.message);
                    return@addSnapshotListener;
                }
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.exists()) {
                    val model = queryDocumentSnapshots.toObject(Story::class.java)
                    model?.postId = queryDocumentSnapshots.id

                    /*  binding.apply {
                          if (model != null) {
                              thumbUpTv.text = model.likedBy.filterValues { it }.keys.size.toString()
                              thumbDownTv.text =
                                  model.likedBy.filterValues { !it }.keys.size.toString()
                          }
                      }*/
                }
            }
    }

    private fun getUserDetails() {

        binding.progressBar.visible()
        db.collection("Users").document(args.story.userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.progressBar.gone()
                val user = task.result.toObject(User::class.java)
                binding.userNameTv.text = user!!.userName
                if (user.image!!.isNotEmpty()) {
                    Glide.with(fragmentContext).load(user.image).into(binding.myProfileIv)
                } else {
                    binding.myProfileIv.setImageResource(R.drawable.place_holder)
                }
            } else {
                binding.progressBar.gone()
                Log.d("LOGGER", "is Fail")
                Toast.makeText(
                    context,
                    task.exception!!.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun timeToMilliseconds(time: String): Long {
        val parts = time.split(":")
        val minutes = parts[0].toLong()
        val seconds = parts[1].toLong()

        return (minutes * 60 + seconds) * 1000
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }
}