package com.usaclean.sportreelzz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.usaclean.sportreelzz.databinding.ActivityMainBinding
import com.usaclean.sportreelzz.ui.auth.login.LoginActivity
import com.usaclean.sportreelzz.ui.auth.signup.SignUpActivity

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            signUpBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,SignUpActivity::class.java))
            }

            signInBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,LoginActivity::class.java))
            }

        }

    }
}