package com.johnkuper.currenciesconverter.extensions

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.onAnimationsFinished(action: () -> Unit) {
    RecyclerViewAnimationsHelper(this, action).subscribe()
}