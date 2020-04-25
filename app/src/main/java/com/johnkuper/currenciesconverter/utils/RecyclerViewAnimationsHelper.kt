package com.johnkuper.currenciesconverter.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener

/**
 * Notifies listeners when all RecyclerView animations are completed. It's helpful to postpone
 * adapter updates until all animations are completed to prevent canceled or stutter animations.
 * Used through [RecyclerView.onAnimationsFinished].
 */
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