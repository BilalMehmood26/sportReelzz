package com.usaclean.sportreelzz.ui.auth.login

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.AlertForgetPasswordLayoutBinding
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

            forgetTV.setOnClickListener {
                showForgetDialog()
            }
        }
    }

    private fun showForgetDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = AlertForgetPasswordLayoutBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        dialogBinding.submitTV.setOnClickListener {
            val email = dialogBinding.emailET.text.toString()

            if (email.isNotEmpty()){
                //mDialog.show()
                /*mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        mDialog.dismiss()
                        Toast.makeText(this, "Please check you email", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        mDialog.dismiss()
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }*/
                Toast.makeText(this, "Please check you email", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else{
                Toast.makeText(this, "Email Required", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

    }
}