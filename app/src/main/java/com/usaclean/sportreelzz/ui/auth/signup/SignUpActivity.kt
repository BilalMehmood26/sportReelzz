package com.usaclean.sportreelzz.ui.auth.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.ActivitySignUpBinding
import com.usaclean.sportreelzz.ui.DashboardActivity

class SignUpActivity : AppCompatActivity() {

    private var _binding: ActivitySignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            loginBtn.setOnClickListener {
                finish()
            }

            signUpBtn.setOnClickListener {
                startActivity(Intent(this@SignUpActivity,DashboardActivity::class.java))
            }
        }
    }
}