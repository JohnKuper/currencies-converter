package com.johnkuper.currenciesconverter.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener

class RecyclerViewAnimationsHelper(private val recyclerView: RecyclerView, onAnimationsFinished: () -> Unit) {

    private val isAnimatingListener = Runnable {
        if (recyclerView.isAnimating) {
            recyclerView.itemAnimator?.isRunning(animationsFinishedListener)
            return@Runnable
        }
        onAnimationsFinished()
    }

    private val animationsFinishedListener: ItemAnimatorFinishedListener = ItemAnimatorFinishedListener {
        recyclerView.post(isAnimatingListener)
    }

    fun subscribe() {
        recyclerView.post(isAnimatingListener)
    }
}