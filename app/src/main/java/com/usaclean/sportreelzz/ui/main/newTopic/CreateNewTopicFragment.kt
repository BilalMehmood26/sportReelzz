package com.usaclean.sportreelzz.ui.main.newTopic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentCreateNewTopicBinding
import com.usaclean.sportreelzz.model.Chapter
import com.usaclean.sportreelzz.ui.main.topicDetails.PreviewDialogFragment
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class CreateNewTopicFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE_VIDEO = 1001
        private const val REQUEST_CODE_PERMISSIONS = 1002
        private const val REQUEST_CODE_RECORD_VIDEO = 1004

        fun newInstance() = CreateNewTopicFragment()
    }

    val db = Firebase.firestore

    private lateinit var fragmentContext: Context
    private var _binding: FragmentCreateNewTopicBinding? = null
    private val binding get() = _binding!!

    private lateinit var previewDialogFragment: PreviewDialogFragment
    private lateinit var videoChaptersDialogFragment: VideoChaptersDialogFragment

    private var videoURI: Uri? = null
    private var videoDuration: Long = 0

    private var selectedLanguage: String = ""
    private var selectedSports: String = ""

    private var languageList = arrayListOf(
        IconSpinnerItem(text = "English", iconRes = R.drawable.ic_english),
        IconSpinnerItem(text = "Dutch", iconRes = R.drawable.ic_dutch)
    )

    private var sportsList = arrayListOf(
        IconSpinnerItem(text = "Football", iconRes = R.drawable.ic_football),
        IconSpinnerItem(text = "Padel", iconRes = R.drawable.ic_padel),
        IconSpinnerItem(text = "Fitness", iconRes = R.drawable.ic_fitness),
        IconSpinnerItem(text = "Tennis", iconRes = R.drawable.ic_tennis),
        IconSpinnerItem(text = "Basketball", iconRes = R.drawable.ic_basketball),
        IconSpinnerItem(text = "Golf", iconRes = R.drawable.ic_golf),
        IconSpinnerItem(text = "Field Hockey", iconRes = R.drawable.ic_hockey),
        IconSpinnerItem(text = "Volleyball", iconRes = R.drawable.ic_volleyball),
        IconSpinnerItem(text = "Badminton", iconRes = R.drawable.ic_badminton),
        IconSpinnerItem(text = "Handball", iconRes = R.drawable.ic_handball),
        IconSpinnerItem(text = "Athletics", iconRes = R.drawable.ic_athletics),
        IconSpinnerItem(text = "Cycling", iconRes = R.drawable.ic_cycling),
        IconSpinnerItem(text = "Rugby", iconRes = R.drawable.ic_rugby),
        IconSpinnerItem(text = "Yoga/Pilates", iconRes = R.drawable.ic_yoga_pilates),
        IconSpinnerItem(text = "Skiing/Snowboarding", iconRes = R.drawable.ic_snowboarding),
        IconSpinnerItem(text = "Dart", iconRes = R.drawable.ic_dart)
    )

    private lateinit var viewModel: CreateNewTopicViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateNewTopicBinding.inflate(inflater)

        binding.apply {

            sportsEt.apply {
                setSpinnerAdapter(IconSpinnerAdapter(this))
                setItems(sportsList)
                getSpinnerRecyclerView().layoutManager = GridLayoutManager(fragmentContext, 1)
            }

            languageEt.apply {
                setSpinnerAdapter(IconSpinnerAdapter(this))
                setItems(languageList)
                getSpinnerRecyclerView().layoutManager = GridLayoutManager(fragmentContext, 1)
            }

            previewBtn.setOnClickListener {
                previewDialogFragment = PreviewDialogFragment(videoURI!!){
                    videoURI = null
                    previewBtn.gone()
                }
                previewDialogFragment.show(childFragmentManager, "")
            }
            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

            mediaPickerIV.setOnClickListener {
                if (hasRequiredPermissions()) {
                    showImageVideoDialog()
                } else {
                    requestPermissions()
                }
            }

            languageEt.setOnSpinnerItemSelectedListener<IconSpinnerItem> { oldIndex, oldItem, newIndex, newText ->
                val language = languageList[newIndex]
                selectedLanguage = language.text.toString()
                languageEt.setText(selectedLanguage)
            }

            sportsEt.setOnSpinnerItemSelectedListener<IconSpinnerItem> { oldIndex, oldItem, newIndex, newText ->
                val sports = sportsList[newIndex]
                selectedSports = sports.text.toString()
                sportsEt.setText(selectedSports)
            }

            createBtn.setOnClickListener {

                val subject = subjectEt.text.toString()
                val question = questionEditText.text.toString()


                when {
                    subject.isEmpty() -> subjectEt.error = "Required"
                    question.isEmpty() -> questionEditText.error = "Required"
                    else -> uploadVideo(subject, question)
                }
            }
        }
        return binding.root
    }

    private fun uploadVideo(subject: String, question: String) {

        if (videoURI != null) {
            binding.progressBar.visible()
            val postId = UUID.randomUUID().toString()
            videoDuration = getDurationFromVideo(videoURI!!)!!

            val videoPathReference =
                FirebaseStorage.getInstance().reference.child("Posts/Video/${postId}.mp4")
            val uploadVideoToStorage = videoPathReference.putFile(videoURI!!)
            uploadVideoToStorage.addOnSuccessListener {

                videoPathReference.downloadUrl.addOnSuccessListener { videoUri ->

                 /*   videoChaptersDialogFragment = VideoChaptersDialogFragment { chapterList ->
                        if (chapterList[chapterList.size - 1].chapterName.equals("")) {
                            chapterList.removeLast()
                            if (!chapterList[0].chapterName.equals("")) {
                                val chapterMap =
                                    chapterList.associate { it.chapterName to it.timeStamp }
                                val videoUrl = videoUri.toString()
                                val postMap = hashMapOf(
                                    "duration" to videoDuration,
                                    "postType" to "video",
                                    "subject" to subject,
                                    "chapters" to chapterMap,
                                    "question" to question,
                                    "language" to selectedLanguage,
                                    "sports" to selectedSports,
                                    "publishDate" to System.currentTimeMillis(),
                                    "userId" to UserSession.user.id,
                                    "postId" to postId,
                                    "views" to 0,
                                    "videoLink" to videoUrl,
                                )

                                FirebaseFirestore.getInstance().collection("Videos")
                                    .document(postId).set(postMap)
                                    .addOnSuccessListener {
                                        binding.progressBar.gone()
                                        videoURI = null
                                        Toast.makeText(
                                            context,
                                            "Upload Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().popBackStack()
                                    }.addOnFailureListener { it2 ->
                                        binding.progressBar.gone()
                                        Log.d("LOGGER", "uploading Error: ${it2.message}")
                                    }
                            } else {
                                val videoUrl = videoUri.toString()
                                val postMap = hashMapOf(
                                    "duration" to videoDuration,
                                    "postType" to "video",
                                    "subject" to subject,
                                    "question" to question,
                                    "language" to selectedLanguage,
                                    "sports" to selectedSports,
                                    "publishDate" to System.currentTimeMillis(),
                                    "userId" to UserSession.user.id,
                                    "postId" to postId,
                                    "views" to 0,
                                    "videoLink" to videoUrl,
                                )

                                FirebaseFirestore.getInstance().collection("Videos")
                                    .document(postId).set(postMap)
                                    .addOnSuccessListener {
                                        binding.progressBar.gone()
                                        videoURI = null
                                        Toast.makeText(
                                            context,
                                            "Upload Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().popBackStack()
                                    }.addOnFailureListener { it2 ->
                                        binding.progressBar.gone()
                                        Log.d("LOGGER", "uploading Error: ${it2.message}")
                                    }

                            }
                        } else {
                            if (!chapterList[0].chapterName.equals("")) {
                                val chapterMap =
                                    chapterList.associate { it.chapterName to it.timeStamp }
                                val videoUrl = videoUri.toString()
                                val postMap = hashMapOf(
                                    "duration" to videoDuration,
                                    "postType" to "video",
                                    "subject" to subject,
                                    "chapters" to chapterMap,
                                    "question" to question,
                                    "language" to selectedLanguage,
                                    "sports" to selectedSports,
                                    "publishDate" to System.currentTimeMillis(),
                                    "userId" to UserSession.user.id,
                                    "postId" to postId,
                                    "views" to 0,
                                    "videoLink" to videoUrl,
                                )

                                FirebaseFirestore.getInstance().collection("Videos")
                                    .document(postId).set(postMap)
                                    .addOnSuccessListener {
                                        binding.progressBar.gone()
                                        videoURI = null
                                        Toast.makeText(
                                            context,
                                            "Upload Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().popBackStack()
                                    }.addOnFailureListener { it2 ->
                                        binding.progressBar.gone()
                                        Log.d("LOGGER", "uploading Error: ${it2.message}")
                                    }
                            } else {
                                val videoUrl = videoUri.toString()
                                val postMap = hashMapOf(
                                    "duration" to videoDuration,
                                    "postType" to "video",
                                    "subject" to subject,
                                    "question" to question,
                                    "language" to selectedLanguage,
                                    "sports" to selectedSports,
                                    "publishDate" to System.currentTimeMillis(),
                                    "userId" to UserSession.user.id,
                                    "postId" to postId,
                                    "views" to 0,
                                    "videoLink" to videoUrl,
                                )

                                FirebaseFirestore.getInstance().collection("Videos")
                                    .document(postId).set(postMap)
                                    .addOnSuccessListener {
                                        binding.progressBar.gone()
                                        videoURI = null
                                        Toast.makeText(
                                            context,
                                            "Upload Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().popBackStack()
                                    }.addOnFailureListener { it2 ->
                                        binding.progressBar.gone()
                                        Log.d("LOGGER", "uploading Error: ${it2.message}")
                                    }
                            }
                        }
                    }
                    videoChaptersDialogFragment.show(childFragmentManager, "")*/

                    val videoUrl = videoUri.toString()
                    val postMap = hashMapOf(
                        "duration" to videoDuration,
                        "postType" to "video",
                        "subject" to subject,
                        "question" to question,
                        "language" to selectedLanguage,
                        "sports" to selectedSports,
                        "publishDate" to System.currentTimeMillis(),
                        "userId" to UserSession.user.id,
                        "postId" to postId,
                        "views" to 0,
                        "videoLink" to videoUrl,
                    )

                    FirebaseFirestore.getInstance().collection("Videos")
                        .document(postId).set(postMap)
                        .addOnSuccessListener {
                            binding.progressBar.gone()
                            videoURI = null
                            Toast.makeText(
                                context,
                                "Upload Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }.addOnFailureListener { it2 ->
                            binding.progressBar.gone()
                            Log.d("LOGGER", "uploading Error: ${it2.message}")
                        }
                }
            }


        } else {
            Toast.makeText(fragmentContext, "Empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getThumbnailFromVideo(videoUri: Uri): Uri? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val thumbnail = retriever.getFrameAtTime()
        try {
            retriever.release()
        } catch (e: IOException) {
            Log.d("LOGGER", "Exception: ${e.message}")
        }

        if (thumbnail != null) {
            // Convert thumbnail to URI
            return getImageUri(thumbnail)
        }

        return null
    }

    fun getImageUri(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "Thumbnail",
            null
        )
        return Uri.parse(path)
    }

    private fun getDurationFromVideo(videoUri: Uri): Long? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

        try {
            retriever.release()
        } catch (e: IOException) {
            Log.d("LOGGER", "Exception: ${e.message}")
        }

        if (duration != null) {
            return duration
        }

        return null
    }

    fun compressImage(filePath: Uri): ByteArray? {
        val options = BitmapFactory.Options()
        options.inSampleSize = 2
        val bitmap = BitmapFactory.decodeFile(filePath.path, options) ?: return null

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        bitmap.recycle()
        val compressedData = outputStream.toByteArray()
        outputStream.close()

        return compressedData
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

    private fun showImageVideoDialog() {
        val options = arrayOf<CharSequence>("Choose from Gallery", "Take Video")

        val builder = AlertDialog.Builder(fragmentContext)
        builder.setTitle("Select Option")
        builder.setItems(options) { _, item ->
            when (item) {
                0 -> openGalleryOrFilePicker()
                1 -> recordVideo()
            }
        }

        builder.show()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateNewTopicViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE_VIDEO && resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = data?.data
            if (selectedMediaUri != null) {
                videoURI = selectedMediaUri
                Log.d("LOgger", "onActivityResult: $videoURI")
                binding.uploadTv.text = "Video Added"
                binding.previewBtn.visible()
                previewDialogFragment = PreviewDialogFragment(videoURI!!){
                    videoURI = null
                    binding.previewBtn.gone()
                }
                previewDialogFragment.show(childFragmentManager, "")
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
                binding.uploadTv.text = "Video Added"
                binding.previewBtn.visible()
                Toast.makeText(
                    fragmentContext,
                    "Video has been uploaded successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            languageEt.dismiss()
            sportsEt.dismiss()
        }
    }
}