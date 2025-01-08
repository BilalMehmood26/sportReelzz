package com.usaclean.sportreelzz.ui.main.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentMyTopicsBinding
import com.usaclean.sportreelzz.databinding.FragmentProfileBinding
import com.usaclean.sportreelzz.ui.auth.login.LoginActivity
import com.usaclean.sportreelzz.ui.main.home.HomeAdapter
import com.usaclean.sportreelzz.ui.main.home.MyHomeFragmentDirections
import com.usaclean.sportreelzz.utils.UserSession
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var fragmentContext: Context

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater)

        binding.apply {


            logoutBtn.setOnClickListener {
                Firebase.auth.signOut()
                startActivity(Intent(fragmentContext, LoginActivity::class.java))
                requireActivity().finish()
            }

            userNameTv.text = UserSession.user.userName
            memberTv.setText("Member Since ${getYearFromTimestamp(UserSession.user.memberSince!!)}")

            if(UserSession.user.image.equals("").not()){
                Glide.with(requireContext()).load(UserSession.user.image).into(profileIv)
            }
            editBtn.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragment2ToEditProfileFragment()
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearFromTimestamp(timestamp: Long): Int {
        val dateTime: ZonedDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
        return dateTime.year
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}