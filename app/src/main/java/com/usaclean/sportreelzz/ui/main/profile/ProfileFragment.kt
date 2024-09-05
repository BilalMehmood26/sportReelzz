package com.usaclean.sportreelzz.ui.main.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentMyTopicsBinding
import com.usaclean.sportreelzz.databinding.FragmentProfileBinding
import com.usaclean.sportreelzz.ui.main.home.HomeAdapter
import com.usaclean.sportreelzz.ui.main.home.MyHomeFragmentDirections

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater)

        binding.apply {

            logoutBtn.setOnClickListener {
                requireActivity().finish()
            }

            editBtn.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragment2ToEditProfileFragment()
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}