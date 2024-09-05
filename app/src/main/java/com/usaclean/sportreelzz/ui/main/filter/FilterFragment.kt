package com.usaclean.sportreelzz.ui.main.filter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentEditProfileBinding
import com.usaclean.sportreelzz.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    companion object {
        fun newInstance() = FilterFragment()
    }

    private lateinit var viewModel: FilterViewModel

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterBinding.inflate(inflater)

        binding.apply {

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FilterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}