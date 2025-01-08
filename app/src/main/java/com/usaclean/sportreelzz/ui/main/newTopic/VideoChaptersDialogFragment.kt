package com.usaclean.sportreelzz.ui.main.newTopic

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentCreateNewTopicBinding
import com.usaclean.sportreelzz.databinding.FragmentVideoChaptersDialogBinding
import com.usaclean.sportreelzz.model.Chapter

class VideoChaptersDialogFragment(private val callBack: (ArrayList<Chapter>) -> Unit) : DialogFragment() {

    private var _binding: FragmentVideoChaptersDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentContext: Context

    private val chaptersList: ArrayList<Chapter> = arrayListOf()
    private lateinit var chaptersAdapter: ChaptersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoChaptersDialogBinding.inflate(inflater)
        chaptersList.add(Chapter())
        binding.apply {
            chapterRv.apply {
                chaptersAdapter = ChaptersAdapter(fragmentContext, chaptersList) {
                    chaptersList.removeAt(it)
                    chaptersAdapter.notifyDataSetChanged()
                }
                adapter = chaptersAdapter
                layoutManager = LinearLayoutManager(
                    fragmentContext,
                    LinearLayoutManager.VERTICAL, false
                )
            }

            addBtn.setOnClickListener {
                chaptersList.add(Chapter())
                Log.d("LOGER", "list: $chaptersList")
                chaptersAdapter.notifyItemInserted(chaptersList.size+1)
            }

            addChapterBtn.setOnClickListener {
                callBack.invoke(chaptersList)
                dismiss()
            }
        }

        return binding.root
    }

    fun convertTimeToMillis(time: String): Long {
        val parts = time.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Time format must be MM:SS")
        }

        val minutes = parts[0].toLongOrNull() ?: 0L
        val seconds = parts[1].toLongOrNull() ?: 0L

        return (minutes * 60 * 1000) + (seconds * 1000)
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}