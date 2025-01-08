package com.usaclean.sportreelzz

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.usaclean.sportreelzz.databinding.ActivityMainBinding
import com.usaclean.sportreelzz.model.User
import com.usaclean.sportreelzz.ui.DashboardActivity
import com.usaclean.sportreelzz.ui.auth.login.LoginActivity
import com.usaclean.sportreelzz.ui.auth.signup.SignUpActivity
import com.usaclean.sportreelzz.utils.LanguagePreference
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone
import com.usaclean.sportreelzz.utils.visible
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val auth = Firebase.auth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LanguagePreference.getLanguage(this@MainActivity).equals("nl")) {
            val locale = Locale("nl")
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } else {
            val locale = Locale("en")
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }


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

    override fun onResume() {
        super.onResume()

    }
    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            binding.progressBar.visible()
            val userId = auth.currentUser!!.uid
            db.collection("Users").document(userId)
                .get().addOnSuccessListener { task ->
                    binding.progressBar.gone()
                    val user = task.toObject(User::class.java)
                    user!!.id = userId
                    UserSession.user = user
                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    intent.putExtra("user","user")
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
                }.addOnFailureListener {
                    binding.progressBar.gone()
                }
        }
    }
}