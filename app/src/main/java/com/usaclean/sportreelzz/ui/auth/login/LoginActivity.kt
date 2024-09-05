package com.usaclean.sportreelzz.ui.auth.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.ui.DashboardActivity
import com.usaclean.sportreelzz.ui.auth.signup.SignUpActivity

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            signInBtn.setOnClickListener {
                startActivity(Intent(this@LoginActivity,DashboardActivity::class.java))
            }

            registerBtn.setOnClickListener {
                startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            }
        }
    }
}