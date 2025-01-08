package com.usaclean.sportreelzz.ui.language

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.usaclean.sportreelzz.MainActivity
import com.usaclean.sportreelzz.R
import com.usaclean.sportreelzz.databinding.ActivityChangeLanguageBinding
import com.usaclean.sportreelzz.ui.DashboardActivity
import com.usaclean.sportreelzz.utils.LanguagePreference
import java.util.Locale

class ChangeLanguageActivity : AppCompatActivity() {

    private var _binding: ActivityChangeLanguageBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChangeLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            backIv.setOnClickListener {
                finish()
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.english_btn -> {
                        setLocale("en")
                    }

                    R.id.french_btn -> {
                        setLocale("nl")
                    }
                }
            }

            materialButton.setOnClickListener {
                startActivity(Intent(this@ChangeLanguageActivity,DashboardActivity::class.java))
                finish()
            }
        }
    }

    private fun setLocale(languageCode: String) {
        LanguagePreference.saveLanguage(this@ChangeLanguageActivity,languageCode)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}