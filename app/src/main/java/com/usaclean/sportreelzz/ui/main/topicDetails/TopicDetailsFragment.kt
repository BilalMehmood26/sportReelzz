package com.usaclean.sportreelzz.ui.main.topicDetails

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentCreateNewTopicBinding
import com.usaclean.sportreelzz.databinding.FragmentTopicDetailsBinding

class TopicDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = TopicDetailsFragment()
    }

    private var _binding: FragmentTopicDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TopicDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopicDetailsBinding.inflate(inflater)

        binding.apply {

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TopicDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}