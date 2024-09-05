package com.usaclean.sportreelzz.ui.main.newTopic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentCreateNewTopicBinding
import com.usaclean.sportreelzz.databinding.FragmentProfileBinding
import com.usaclean.sportreelzz.ui.main.profile.ProfileFragmentDirections

class CreateNewTopicFragment : Fragment() {

    companion object {
        fun newInstance() = CreateNewTopicFragment()
    }

    private var _binding: FragmentCreateNewTopicBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreateNewTopicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateNewTopicBinding.inflate(inflater)

        binding.apply {

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

            createBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateNewTopicViewModel::class.java)
        // TODO: Use the ViewModel
    }

}