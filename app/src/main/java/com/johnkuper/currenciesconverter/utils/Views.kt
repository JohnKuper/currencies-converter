package com.johnkuper.currenciesconverter.utils

import android.app.Activity.INPUT_METHOD_SERVICE
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.updateLayoutParams
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

fun View.updateMargins(
    topMargin: Int? = null,
    bottomMargin: Int? = null,
    startMargin: Int? = null,
    endMargin: Int? = null
) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        topMargin?.let { this.topMargin = it }
        bottomMargin?.let { this.bottomMargin = it }
        startMargin?.let { this.marginStart = it }
        endMargin?.let { this.marginEnd = it }
    }
}
