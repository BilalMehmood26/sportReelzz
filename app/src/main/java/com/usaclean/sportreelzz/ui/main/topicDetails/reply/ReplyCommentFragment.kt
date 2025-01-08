package com.usaclean.sportreelzz.ui.main.topicDetails.reply

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentReplyCommentBinding
import com.usaclean.sportreelzz.model.Comments
import com.usaclean.sportreelzz.ui.main.topicDetails.CommentAdapter
import com.usaclean.sportreelzz.ui.main.topicDetails.TopicDetailsFragment
import com.usaclean.sportreelzz.ui.main.topicDetails.TopicDetailsFragmentDirections
import com.usaclean.sportreelzz.ui.main.topicDetails.videoActivity.VideoViewActivity
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.util.UUID

class ReplyCommentFragment : Fragment() {

    val db = Firebase.firestore

    private lateinit var fragmentContext: Context
    private var _binding: FragmentReplyCommentBinding? = null
    private val binding get() = _binding!!

    private var videoURI: Uri? = null

    private val TAG = "Reply_Comment_Fragment"

    private lateinit var adapter: CommentReplyAdapter
    private var commentsList: ArrayList<Comments?>? = ArrayList()

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

    private val args: ReplyCommentFragmentArgs by navArgs()

    companion object {

        private const val REQUEST_CODE_SELECT_IMAGE_VIDEO = 1001
        private const val REQUEST_CODE_PERMISSIONS = 1002
        private const val REQUEST_CODE_CAPTURE_IMAGE = 1003
        private const val REQUEST_CODE_RECORD_VIDEO = 1004
        fun newInstance() = ReplyCommentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReplyCommentBinding.inflate(inflater)

        binding.apply {

            getCommentReplyData()
            FirebaseFirestore.getInstance().collection("Users")
                .document(args.comment!!.fromId).get()
                .addOnSuccessListener {
                    var fName = it.getString("userName")
                    var imageUrl = it.getString("image")

                    userNameTV.text = "$fName"
                    if (imageUrl != null && imageUrl != "") {
                        Glide.with(fragmentContext).load(imageUrl)
                            .error(R.drawable.place_holder)
                            .placeholder(R.drawable.place_holder)
                            .into(profileIV)
                    }
                }

            thumbUpTV.text = args.comment.likedBy.filterValues { it }.keys.size.toString()
            contentTV.text = args.comment.content
            timeStampTv.text = convertMillisecondsToTimeFormat(args.comment.videoTimeStamp)
            timeStampTv.paintFlags = timeStampTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            if (args.comment.videoLink.isEmpty()) {
                videoIv.gone()
                playBtn.gone()
            } else {
                videoIv.visible()
                playBtn.visible()
                Glide.with(fragmentContext).load(args.comment.videoLink).into(videoIv)
            }

            if (args.comment.type.equals("image")) {
                playBtn.gone()
            }


            attachmentIv.setOnClickListener {
                if (hasRequiredPermissions()) {
                    showImageVideoDialog()
                } else {
                    requestPermissions()
                }
            }

            sendIv.setOnClickListener {
                val commentText = binding.commentEt.text.toString()
                if (commentText.isEmpty()) {
                    Toast.makeText(fragmentContext, "Field Empty", Toast.LENGTH_SHORT).show()
                } else {
                    progressBar.visible()
                    val replyId = UUID.randomUUID().toString()
                    val commentMap = hashMapOf(
                        "replyId" to replyId,
                        "content" to commentText,
                        "timeStamp" to System.currentTimeMillis(),
                        "fromId" to UserSession.user.id,
                        "type" to "text"
                    )

                    FirebaseFirestore.getInstance().collection("Videos")
                        .document(args.postID).collection("Comments")
                        .document(args.comment.commentId).collection("reply")
                        .document(replyId).set(commentMap)
                        .addOnSuccessListener {
                            progressBar.gone()
                            binding.commentEt.setText("")

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

            sendIv.setOnClickListener {

                val commentText = commentEt.text.toString()
                progressBar.visible()
                when {
                    videoURI.toString().contains(".mp4") -> {
                        uploadCommentWithMedia(
                            "Posts/Comment/Video/${args.comment.commentId}.mp4",
                            "video",
                            commentText
                        )
                    }
                    videoURI.toString().contains(".jpg") -> {
                        uploadCommentWithMedia(
                            "Posts/Comment/Image/${args.comment.commentId}.jpg",
                            "image",
                            commentText
                        )
                    }
                    else -> {
                        uploadCommentText(commentText)
                    }
                }
            }

            videoIv.setOnClickListener {
                val intent = Intent(context, VideoViewActivity::class.java)
                intent.putExtra("URL", args.comment.videoLink)
                intent.putExtra("type", args.comment.type)
                fragmentContext.startActivity(intent)
            }
        }
        setAdapter()

        return binding.root
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE_VIDEO && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data
            if (selectedMediaUri != null) {
                videoURI = selectedMediaUri
                Log.d("LOgger", "onActivityResult: $videoURI")
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

    private fun getCommentReplyData() {
        commentsList!!.clear()
        binding.progressBar.visible()
        FirebaseFirestore.getInstance().collection("Videos")
            .document(args.postID).collection("Comments").document(args.comment.commentId).collection("reply")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { queryDocumentSnapshots, error ->
                if (error != null) {
                    Log.d("LOGGER", "Exception " + error.message);
                    binding.progressBar.gone()
                    return@addSnapshotListener
                }
                commentsList!!.clear()
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val model = documentSnapshot.toObject(Comments::class.java)
                        commentsList!!.add(model)
                        Log.d("LOGGER", "getCommentData: ${commentsList!!.size}")
                    }
                    binding.progressBar.gone()
                    setAdapter()
                }
                binding.progressBar.gone()

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

    private fun saveCommentToFirestore(commentMap: Map<String, Any>) {
        val postId = args.comment.commentId
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

    private fun createCommentMap(content: String, type: String, mediaLink: String?): Map<String, Any> {
        val replyId = UUID.randomUUID().toString()
        return hashMapOf(
            "replyId" to replyId,
            "content" to content,
            "timeStamp" to System.currentTimeMillis(),
            "fromId" to UserSession.user.id,
            "type" to type
        ).apply {
            mediaLink?.let { this["videoLink"] = it }
        }
    }

    private fun recordVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent,REQUEST_CODE_RECORD_VIDEO)
    }

    private fun openGalleryOrFilePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_VIDEO)
    }

    private fun openImageOrFilePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQUEST_CODE_CAPTURE_IMAGE)
    }

    private fun requestPermissions() {
        requestPermissions(requiredPermissions,REQUEST_CODE_PERMISSIONS)
    }

    private fun setAdapter() {
        binding.apply {
            val layoutManager =
                LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
            commentRv.layoutManager = layoutManager
            val reversedCommentsList = commentsList!!.reversed()
            adapter = CommentReplyAdapter(fragmentContext, reversedCommentsList)
            commentRv.adapter = adapter
        }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}