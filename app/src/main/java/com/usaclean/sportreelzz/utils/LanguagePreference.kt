package com.usaclean.sportreelzz.utils

import android.content.Context
import android.content.SharedPreferences

object LanguagePreference {
    private const val PREF_NAME = "language_pref"
    private const val KEY_LANGUAGE = "language_key"

    fun saveLanguage(context: Context, languageCode: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LANGUAGE, languageCode)
        editor.apply()
    }

    fun getLanguage(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_LANGUAGE, "en")
    }
}