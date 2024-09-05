package com.usaclean.sportreelzz.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityDashboardBinding
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible

class DashboardActivity : AppCompatActivity() {

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.main_fragment_Container) as NavHostFragment
            navController = navHostFragment.navController
            bottomNavigationView2.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.myHomeFragment -> {
                        bottomNavigationView2.visible()
                    }

                    R.id.myTopicsFragment -> {
                        bottomNavigationView2.visible()
                    }

                    R.id.profileFragment2 -> {
                        bottomNavigationView2.visible()
                    }

                    else -> {
                        bottomNavigationView2.gone()
                    }
                }
            }

        }
    }
}