package com.usaclean.sportreelzz.ui.main.profile.editProfile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentEditProfileBinding
import com.usaclean.sportreelzz.databinding.FragmentProfileBinding
import com.usaclean.sportreelzz.ui.main.profile.ProfileFragmentDirections

class EditProfileFragment : Fragment() {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    private lateinit var viewModel: EditProfileViewModel

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater)

        binding.apply {

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

            saveBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}