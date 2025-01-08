package com.usaclean.sportreelzz.ui.main.profile.editProfile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.FragmentEditProfileBinding
import com.usaclean.sportreelzz.databinding.FragmentProfileBinding
import com.usaclean.sportreelzz.ui.main.profile.ProfileFragmentDirections
import com.usaclean.sportreelzz.utils.UserSession
import java.util.UUID

class EditProfileFragment : Fragment() {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    private lateinit var viewModel: EditProfileViewModel

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!


    lateinit var imageURI: Uri
    private val mAuth = Firebase.auth
    private var db = Firebase.firestore

    lateinit var mFirestore: FirebaseFirestore

    lateinit var mDialog: ProgressDialog
    private lateinit var fragmentContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater)

        binding.apply {

            firstNameTv.setText("${UserSession.user.userName}")
            emailTv.setText(UserSession.user.email)
            passwordTv.setText(UserSession.user.password)
            if (!UserSession.user.image.equals("")) {
                Glide.with(fragmentContext).load(UserSession.user.image).into(profileIv)
            }

            profileIv.setOnClickListener {
                openGalleryOrFilePicker()
            }

            backIv.setOnClickListener {
                findNavController().popBackStack()
            }

            saveBtn.setOnClickListener {
                saveInfo(
                    firstNameTv.text.toString(),
                    passwordTv.text.toString(),
                    emailTv.text.toString()
                )
            }
        }
        return binding.root
    }



    private fun saveInfo(
        userName: String,
        password: String,
        email: String
    ) {

        binding.progressBar.visibility = View.VISIBLE
        if (imageURI != null) {
            val storage =
                FirebaseStorage.getInstance().reference.child("Users/${mAuth.currentUser!!.uid}.jpg")

            var uploadTask = storage.putFile(imageURI!!)
            uploadTask.addOnSuccessListener {
                storage.downloadUrl.addOnSuccessListener {
                    val userUpdate = hashMapOf(
                        "userName" to userName,
                        "password" to password,
                        "email" to email,
                        "image" to it.toString()
                    ) as Map<String, Any>
                    UserSession.user.image = it.toString()

                    db.collection("Users").document(mAuth.currentUser!!.uid).update(userUpdate)
                        .addOnSuccessListener {
                            UserSession.user.userName = userName
                            UserSession.user.email = email
                            UserSession.user.password = password
                            Toast.makeText(fragmentContext, "Profile Updated", Toast.LENGTH_SHORT).show()
                            if(password.isNotEmpty()){
                                updatePassword(password)
                            }
                        }.addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                fragmentContext,
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(fragmentContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val userUpdate = hashMapOf(
                "firstName" to userName,
                "password" to password,
                "email" to email,
            ) as Map<String, Any>
            db.collection("Users").document(mAuth.currentUser!!.uid).update(userUpdate)
                .addOnSuccessListener {
                    UserSession.user.userName = userName
                    UserSession.user.email = email
                    UserSession.user.password = password
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(fragmentContext, "Profile Updated", Toast.LENGTH_SHORT)
                        .show()
                    if(password.isNotEmpty()){
                        updatePassword(password)
                    }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        fragmentContext,
                        it.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun openGalleryOrFilePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Glide.with(fragmentContext).load(data?.data).into(binding.profileIv)
            imageURI = data!!.data!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(fragmentContext, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePassword(password: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential =
            EmailAuthProvider.getCredential(UserSession.user.email!!, UserSession.user.password!!)
        Firebase.auth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    Firebase.auth.currentUser!!.updatePassword(password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.progressBar.visibility = View.GONE
                                Log.d("LOGGER!", "Password updated successfully.")
                            } else {
                                binding.progressBar.visibility = View.GONE
                                Log.e(
                                    "LOGGER!",
                                    "Error updating password: ${task.exception?.message}"
                                )
                                Toast.makeText(
                                    fragmentContext,
                                    "${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        fragmentContext,
                        "${reauthTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LOGGER!", "Re-authentication failed: ${reauthTask.exception?.message}")
                }
            }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}