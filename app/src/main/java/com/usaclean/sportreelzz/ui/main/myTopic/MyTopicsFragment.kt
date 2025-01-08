package com.usaclean.sportreelzz.ui.main.myTopic

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentMyHomeBinding
import com.usaclean.sportreelzz.databinding.FragmentMyTopicsBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding
import com.usaclean.sportreelzz.model.Story
import com.usaclean.sportreelzz.ui.main.filter.FilterFragment
import com.usaclean.sportreelzz.ui.main.home.HomeAdapter
import com.usaclean.sportreelzz.ui.main.home.MyHomeFragmentDirections
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible

class MyTopicsFragment : Fragment() {

    companion object {
        fun newInstance() = MyTopicsFragment()
    }

    private var _binding: FragmentMyTopicsBinding? = null
    private val binding get() = _binding!!

    private lateinit var fragmentContext: Context
    private lateinit var homeAdapter: HomeAdapter

    private val storyList: ArrayList<Story> = ArrayList()
    private lateinit var filterFragmentDialog: FilterFragment
    private lateinit var viewModel: MyTopicsViewModel
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyTopicsBinding.inflate(inflater)

        binding.apply {

            getVideoList()

            filterIv.setOnClickListener {
                filterFragmentDialog = FilterFragment { subject, question, sports, language ->
                }
                filterFragmentDialog.show(childFragmentManager, "")
            }
        }
        return binding.root
    }

    private fun getVideoList() {
        Log.d("Logger", "getVideoList: ")
        binding.progressBar.visible()
        db.collection("Videos").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(fragmentContext, error.message.toString(), Toast.LENGTH_SHORT).show()
                binding.progressBar.gone()
                return@addSnapshotListener
            }

            binding.progressBar.gone()
            storyList.clear()
            value!!.forEach {
                Log.d("Logger", "getVideoList: LOOP ")
                val video = it.toObject(Story::class.java)
                if (video.userId.equals(Firebase.auth.currentUser!!.uid))
                    storyList.add(video)
            }
            updateAdapter()
        }
    }

    private fun updateAdapter() {
        binding.apply {
            stroyRv.apply {
                homeAdapter = HomeAdapter(storyList, fragmentContext, itemClick = {
                    val action = MyTopicsFragmentDirections.actionMyTopicsFragmentToTopicDetailsFragment(it)
                    findNavController().navigate(action)
                }, editDetails = {

                }, reportCLick = {

                })
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(
                    fragmentContext,
                    LinearLayoutManager.VERTICAL, false
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MyTopicsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}