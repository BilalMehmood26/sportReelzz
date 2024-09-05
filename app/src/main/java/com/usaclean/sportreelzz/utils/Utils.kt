package com.usaclean.sportreelzz.utils

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

fun ImageView.setTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(
        this,
        ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
    )
}

fun TextView.setColor(@ColorRes resId: Int) {
    this.setTextColor(ContextCompat.getColor(context, resId))
}


fun View.hideKeyboard() {
    try {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    } catch (e: Exception) {

    }
}

fun hideKeyboard(view: View, context: Context) {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {

    }
}

fun View.setKeyboardHideOnClickListener() {
    this.setOnClickListener {
        it.hideKeyboard()
    }
}

fun String.isPasswordLength() = length >= 8

fun String.hasUpperCaseChar(): Boolean {
    return this.contains("[A-Z]".toRegex())
}

fun String.hasLowerCaseChar(): Boolean {
    return this.contains("[a-z]".toRegex())
}

fun String.hasNumberOrSymbols() = hasNumber() || hasSpecialCharacters()

fun String.hasNumber(): Boolean {
    return this.contains("\\d".toRegex())
}

fun String.hasSpecialCharacters() = contains("[^\\w]".toRegex())
fun String.isValidPassword() =
    isPasswordLength() &&
            hasUpperCaseChar() &&
            hasLowerCaseChar() &&
            hasNumberOrSymbols()

fun View?.gone() {
    this?.let { it.visibility = View.GONE }
}

fun View?.visible() {
    this?.let { it.visibility = View.VISIBLE }
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible(flag: Boolean) {
    visibility = if (flag) View.VISIBLE else View.GONE
}

fun goneViews(vararg views: View) {
    views.forEach { it.gone() }
}

fun visibleViews(vararg views: View) {
    views.forEach { it.visible() }
}

fun invisibleViews(vararg views: View) {
    views.forEach { it.inVisible() }
}

fun postDelayed(ms: Long, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        action.invoke()
    }, ms)
}