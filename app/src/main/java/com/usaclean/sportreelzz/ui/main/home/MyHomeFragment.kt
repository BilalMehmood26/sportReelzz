package com.usaclean.sportreelzz.ui.main.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.FragmentMyHomeBinding

class MyHomeFragment : Fragment() {

    companion object {
        fun newInstance() = MyHomeFragment()
    }

    private var _binding: FragmentMyHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter

    private lateinit var viewModel: MyHomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyHomeBinding.inflate(inflater)

        binding.apply {

            homeAdapter = HomeAdapter(itemClick = {
                val action = MyHomeFragmentDirections.actionMyHomeFragmentToTopicDetailsFragment()
                findNavController().navigate(action)
            })

            filterIv.setOnClickListener {
                val action = MyHomeFragmentDirections.actionMyHomeFragmentToFilterFragment()
                findNavController().navigate(action)
            }

            materialButton.setOnClickListener {
                val action = MyHomeFragmentDirections.actionMyHomeFragmentToCreateNewTopicFragment()
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
        viewModel = ViewModelProvider(this).get(MyHomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}