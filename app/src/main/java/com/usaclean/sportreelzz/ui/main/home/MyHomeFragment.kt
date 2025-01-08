package com.usaclean.sportreelzz.ui.main.home

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.AlertForgetPasswordLayoutBinding
import com.usaclean.sportreelzz.databinding.FragmentFilterBinding
import com.usaclean.sportreelzz.databinding.FragmentMyHomeBinding
import com.usaclean.sportreelzz.databinding.LayoutReportBinding
import com.usaclean.sportreelzz.model.Filter
import com.usaclean.sportreelzz.model.Story
import com.usaclean.sportreelzz.ui.language.ChangeLanguageActivity
import com.usaclean.sportreelzz.ui.main.filter.FilterFragment
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import io.paperdb.Paper
import java.util.Locale
import kotlin.math.log

class MyHomeFragment : Fragment() {

    companion object {
        fun newInstance() = MyHomeFragment()
    }

    private var _binding: FragmentMyHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentContext: Context

    private val storyList: ArrayList<Story> = ArrayList()


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
        IconSpinnerItem(text = "Skiing/Snowboarding", iconRes = R.drawable.ic_snowboarding)
    )

    private var selectedLanguage: String = ""
    private var selectedSports: String = ""

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var homeAdapter: HomeAdapter

    private lateinit var viewModel: MyHomeViewModel
    private lateinit var filterFragmentDialog: FilterFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyHomeBinding.inflate(inflater)

        binding.apply {

            getVideoList()
            Paper.init(fragmentContext)
            filterIv.setOnClickListener {
                filterFragmentDialog =
                    FilterFragment { subject, question, sportsList, languageList ->
                        getFilteredList(subject, question, sportsList, languageList)
                    }
                filterFragmentDialog.show(childFragmentManager, "")
            }

            materialButton.setOnClickListener {
                val action = MyHomeFragmentDirections.actionMyHomeFragmentToCreateNewTopicFragment()
                findNavController().navigate(action)
            }

            languageIv.setOnClickListener {
                startActivity(Intent(fragmentContext, ChangeLanguageActivity::class.java))
            }

        }
        return binding.root
    }


    private fun editDetailsDialog(videoDetails: Story) {
        val dialog = Dialog(fragmentContext)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = FragmentFilterBinding.inflate(LayoutInflater.from(fragmentContext))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        dialogBinding.apply {
            filterTv.setText("Edit Details")
            languageEt.setText(videoDetails.language)
            sportsEt.setText(videoDetails.sports)
            subjectEt.setText(videoDetails.subject)
            questionEditText.setText(videoDetails.question)
            showResultBtn.setText(getString(R.string.update_info))
            selectedLanguage = videoDetails.language
            selectedSports = videoDetails.sports

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

            showResultBtn.setOnClickListener {
                updateInfo(
                    subjectEt.text.toString(),
                    questionEditText.text.toString(),
                    selectedLanguage,
                    selectedSports,
                    videoDetails.postId
                )
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateInfo(
        subject: String,
        question: String,
        language: String,
        sports: String,
        postID: String
    ) {

        binding.progressBar.visible()
        val updateInfo = hashMapOf(
            "subject" to subject,
            "question" to question,
            "language" to language,
            "sports" to sports,
        )

        db.collection("Videos").document(postID).update(updateInfo as Map<String, Any>)
            .addOnSuccessListener {
                binding.progressBar.gone()
                Toast.makeText(fragmentContext, "Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                binding.progressBar.visible()
                Toast.makeText(fragmentContext, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFilteredList(
        subject: String,
        question: String,
        sports: List<Filter>,
        language: List<Filter>
    ) {

        val filteredList = storyList.filter { video ->
            val sportsMatch = sports.isEmpty() || sports.any { filter ->
                video.sports.contains(filter.title, ignoreCase = true)
            }
            val languageMatch = language.isEmpty() || language.any { filter ->
                video.language.contains(filter.title, ignoreCase = true)
            }

            (subject.isEmpty() || video.subject.contains(subject, ignoreCase = true)) &&
                    (question.isEmpty() || video.question.contains(question, ignoreCase = true)) && sportsMatch && languageMatch
        }

        updateAdapter(filteredList)
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
                storyList.add(video)
            }
            updateAdapter(storyList, isNewFiltered = false)
        }
    }

    private fun updateAdapter(storyList: List<Story>, isNewFiltered: Boolean = true) {
        if (!isNewFiltered) {
            val subject = Paper.book().read("subject", "")
            val question = Paper.book().read("question", "")
            val sports: List<Filter> = Paper.book().read("sportsList", emptyList())!!
            val language: List<Filter> = Paper.book().read("languageList", emptyList())!!

            val filteredList = storyList.filter { video ->
                val sportsMatch = sports.isEmpty() || sports.any { filter ->
                    video.sports.contains(filter.title, ignoreCase = true)
                }
                val languageMatch = language.isEmpty() || language.any { filter ->
                    video.language.contains(filter.title, ignoreCase = true)
                }

                (subject!!.isEmpty() || video.subject.contains(subject, ignoreCase = true)) &&
                        (question!!.isEmpty() || video.question.contains(
                            question,
                            ignoreCase = true
                        )) &&
                        sportsMatch &&
                        languageMatch
            }

            initAdapter(filteredList)
        } else {
            initAdapter(storyList)
        }
    }

    private fun initAdapter(storyList: List<Story>) {

        binding.apply {
            storyRv.apply {
                homeAdapter = HomeAdapter(storyList, fragmentContext, itemClick = {
                    val action =
                        MyHomeFragmentDirections.actionMyHomeFragmentToTopicDetailsFragment(it)
                    findNavController().navigate(action)
                }, editDetails = {
                    editDetailsDialog(it)
                }, reportCLick = {
                    reportDialog(it)
                })
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(
                    fragmentContext,
                    LinearLayoutManager.VERTICAL, false
                )
            }
        }
    }

    private fun reportDialog(videoDetails: Story) {
        val dialog = Dialog(fragmentContext)
        var selectedText = ""
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = LayoutReportBinding.inflate(LayoutInflater.from(fragmentContext))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        dialogBinding.apply {
            cancelBtn.setOnClickListener {
                dialog.hide()
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
                selectedText = selectedRadioButton?.text.toString()
            }

            reportTv.setOnClickListener {
                reportVideo(videoDetails.userId, videoDetails.postId, selectedText)
                dialog.hide()
            }
        }

        dialog.show()
    }

    private fun reportVideo(userID: String, postID: String, reason: String) {
        binding.progressBar.visible()
        val reportVideo = hashMapOf(
            "postID" to postID,
            "userID" to userID,
            "reason" to reason
        )
        db.collection("ReportVideo").document(userID).set(reportVideo).addOnSuccessListener {
            binding.progressBar.gone()
            Toast.makeText(fragmentContext, "Reported", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBar.gone()
            Toast.makeText(fragmentContext, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}