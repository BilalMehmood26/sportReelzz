package com.usaclean.sportreelzz.ui.auth.login

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityLoginBinding
import com.usaclean.sportreelzz.databinding.AlertForgetPasswordLayoutBinding
import com.usaclean.sportreelzz.model.User
import com.usaclean.sportreelzz.ui.DashboardActivity
import com.usaclean.sportreelzz.ui.auth.signup.SignUpActivity
import com.usaclean.sportreelzz.utils.LocationUtility
import com.usaclean.sportreelzz.utils.UserSession
import com.usaclean.sportreelzz.utils.gone

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var locationUtility: LocationUtility

    private val RC_SIGN_IN = 100
    private var userLat = 0.0
    private var userLng = 0.0
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1076746183604-5umj2odp24s66qtemltoj1d840kj39kt.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        locationUtility = LocationUtility(this@LoginActivity)

        binding.apply {
            signInBtn.setOnClickListener {
                val email = binding.emailEt.text.toString()
                val password = binding.passwordEt.text.toString()

                when {
                    email.isEmpty() -> binding.emailEt.error = "Required"
                    password.isEmpty() -> binding.passwordEt.error = "Required"
                    else -> login(email, password)
                }
               //
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

            googleSignin.setOnClickListener {
                mGoogleSignInClient.signOut().addOnCompleteListener(this@LoginActivity) {
                    val signInIntent = mGoogleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }

            registerBtn.setOnClickListener {
                startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            }

            forgetTV.setOnClickListener {
                showForgetDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            binding.progressBar.visibility = View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    val idToken = account.idToken

                    FirebaseMessaging.getInstance().token.addOnCompleteListener {
                        token = it.result
                        Log.d("GoogleSignIn", "token : $token");
                    }
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            val userId = auth.currentUser?.uid.toString()

                            Log.d("GoogleSignIn", "Google Auth Success: $userId")
                            db.collection("Users").document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        Log.d("GoogleSignIn", "Google Auth User Exists")
                                        getUserDetails(userId)
                                    } else {
                                        Log.d("GoogleSignIn", "Google Auth New User")
                                        userDetails(
                                            account.displayName.toString(),
                                            account.email.toString(),
                                            token,
                                            account.idToken!!,
                                            account.photoUrl.toString()
                                        )
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d(
                                        "GoogleSignIn",
                                        "Error fetching user: ${exception.message}"
                                    )
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Failed to fetch user data.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("GoogleSignIn", "Google Auth Failed: ${exception.message}")
                            binding.progressBar.gone()
                            Toast.makeText(
                                this@LoginActivity,
                                "Authentication Failed: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
            } catch (e: ApiException) {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.gone()
                Log.e("GoogleSignIn", "Google sign-in failed with ApiException: ${e.statusCode}")
                e.printStackTrace()
            }
        }
    }

    private fun userDetails(fullName: String, email: String, password: String, token: String,image:String) {

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
                "image" to image,
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
                    Toast.makeText(this@LoginActivity, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            locationUtility.removeLocationUpdates()
        }
    }

    private fun login(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener {
                    updateToken(it, task.result.user!!.uid)
                }.addOnFailureListener {
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                    getUserDetails(task.result.user!!.uid)
                    Log.d("LOGGER", "token: ${it.message.toString()}")
                }
            } else {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                Log.d("LOGGER", "login: ${task?.exception?.message.toString()}")
            }
        }
    }

    private fun updateToken(token: String, userID: String) {
        val user = hashMapOf(
            "token" to token,
            "isOnline" to true
        ) as Map<String, Any>
        db.collection("Users").document(userID).update(user)
            .addOnSuccessListener {
                getUserDetails(userID)
            }.addOnFailureListener {
                Toast.makeText(this@LoginActivity, it.message.toString(), Toast.LENGTH_SHORT).show()
                Log.d("LOGGER", "token FailureListener: ${it.message.toString()}")
                getUserDetails(userID)
            }
    }

    private fun getUserDetails(uid: String) {
        db.collection("Users").document(uid).get().addOnSuccessListener { response ->

            if (response.exists()) {
                binding.progressBar.visibility = View.GONE
               val user = response.toObject(User::class.java)
                UserSession.user = user!!
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
            }
        }.addOnFailureListener { error ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
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
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Please check you email", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "Please check you email", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }else{
                Toast.makeText(this, "Email Required", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

    }
}