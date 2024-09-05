package com.usaclean.sportreelzz.ui.main.myTopic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentMyHomeBinding
import com.usaclean.sportreelzz.databinding.FragmentMyTopicsBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding
import com.usaclean.sportreelzz.ui.main.home.HomeAdapter
import com.usaclean.sportreelzz.ui.main.home.MyHomeFragmentDirections

class MyTopicsFragment : Fragment() {

    companion object {
        fun newInstance() = MyTopicsFragment()
    }

    private var _binding: FragmentMyTopicsBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter

    private lateinit var viewModel: MyTopicsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyTopicsBinding.inflate(inflater)

        binding.apply {

            homeAdapter = HomeAdapter(itemClick = {
                val action = MyTopicsFragmentDirections.actionMyTopicsFragmentToTopicDetailsFragment()
                findNavController().navigate(action)
            })

            filterIv.setOnClickListener {
                val action = MyTopicsFragmentDirections.actionMyTopicsFragmentToFilterFragment()
                findNavController().navigate(action)
            }

            stroyRv.apply {
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.VERTICAL, false
                )
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MyTopicsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}