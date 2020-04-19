package com.johnkuper.currenciesconverter.utils

import android.app.Activity.INPUT_METHOD_SERVICE
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.onAnimationsFinished(action: () -> Unit) {
    RecyclerViewAnimationsHelper(this, action).subscribe()
}

private fun Context.getInputMethodManager() = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

fun View.showKeyboard() {
    context.getInputMethodManager().showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    context.getInputMethodManager().hideSoftInputFromWindow(windowToken, 0)
}

