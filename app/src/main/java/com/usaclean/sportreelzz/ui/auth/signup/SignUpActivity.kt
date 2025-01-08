package com.usaclean.sportreelzz.ui.auth.signup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.ActivitySignUpBinding
import com.usaclean.sportreelzz.model.User
import com.usaclean.sportreelzz.ui.DashboardActivity
import com.usaclean.sportreelzz.utils.LocationUtility
import com.usaclean.sportreelzz.utils.UserSession

class SignUpActivity : AppCompatActivity() {

    private var _binding: ActivitySignUpBinding? = null
    private val binding get() = _binding!!

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var locationUtility: LocationUtility
    val REQUEST_CODE = 1000
    private var userLat = 0.0
    private var userLng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (ActivityCompat.checkSelfPermission(
                this@SignUpActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@SignUpActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this@SignUpActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE
            )
        }
        locationUtility = LocationUtility(this@SignUpActivity)

        binding.apply {
            loginBtn.setOnClickListener {
                finish()
            }

            passwordToggleIV.setTag(R.drawable.ic_visibility_off_)
            passwordToggleIV.setOnClickListener { view ->
                if (passwordToggleIV.getTag() as Int == R.drawable.ic_visibility_off_) {
                    passwordToggleIV.setImageResource(R.drawable.ic_visibility_on)
                    passwordToggleIV.setTag(R.drawable.ic_visibility_on)
                    passwordEt.setInputType(
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    )
                } else {
                    passwordToggleIV.setImageResource(R.drawable.ic_visibility_off_)
                    passwordToggleIV.setTag(R.drawable.ic_visibility_off_)
                    passwordEt.setInputType(
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                    )
                }
                val textLength: Int = passwordEt.getText().length
                passwordEt.setSelection(textLength)
            }

            signUpBtn.setOnClickListener {
                val email = emailEt.text.toString()
                val userName = userNameEt.text.toString()
                val password = passwordEt.text.toString()

                when {
                    userName.isEmpty() -> userNameEt.error = "Required"
                    email.isEmpty() -> emailEt.error = "Required"
                    password.isEmpty() -> passwordEt.error = "Required"
                    else -> signUp(userName, email, password)
                }
                //startActivity(Intent(this@SignUpActivity, DashboardActivity::class.java))
            }
        }
    }

    private fun signUp(fullName: String, email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                userDetails(fullName, email, password, it)
            }.addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                userDetails(fullName, email, password, "")
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@SignUpActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun userDetails(fullName: String, email: String, password: String, token: String) {

        locationUtility.requestLocationUpdates { currentLocation ->
            userLat = currentLocation.latitude
            userLng = currentLocation.longitude

            val memberSince = System.currentTimeMillis()
            val user = hashMapOf(
                "email" to email,
                "id" to Firebase.auth.currentUser!!.uid,
                "userName" to fullName,
                "token" to token,
                "userLat" to userLat,
                "userLng" to userLng,
                "memberSince" to memberSince,
                "password" to password
            )
            var userModel = User(
                email = email,
                id = Firebase.auth.currentUser!!.uid,
                userName = fullName,
                token = token,
                lat = userLat,
                lng = userLng,
                memberSince = memberSince,
                password = password
            )

            db.collection("Users").document(Firebase.auth.currentUser!!.uid).set(user)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    UserSession.user = userModel
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@SignUpActivity, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            locationUtility.removeLocationUpdates()
        }
    }
}