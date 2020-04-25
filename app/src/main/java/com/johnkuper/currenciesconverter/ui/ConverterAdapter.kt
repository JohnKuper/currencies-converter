package com.johnkuper.currenciesconverter.ui

import android.content.Context
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.utils.*
import kotlinx.android.synthetic.main.converter_list_item.view.*

class ConverterAdapter(
    context: Context,
    private val converterItemsListener: ConverterItemsListener
) : RecyclerView.Adapter<ConverterAdapter.ConverterViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private val items = mutableListOf<ConverterItem>()
    private val inflater = LayoutInflater.from(context)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConverterViewHolder {
        return ConverterViewHolder(inflater.inflate(R.layout.converter_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ConverterViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return items[position].currencyCode.hashCode().toLong()
    }

    private fun moveItemOnTop(fromPosition: Int) {
        if (fromPosition > 0) {
            val newTop = items[fromPosition]
            items.removeAt(fromPosition)
            items.add(0, newTop)
            notifyItemMoved(fromPosition, 0)
            recyclerView.scrollToPosition(0)
            converterItemsListener.onItemsChanged(items)
        }
    }

    fun setData(newItems: List<ConverterItem>) {
        items.clear()
        items.addAll(newItems)
        recyclerView.onAnimationsFinished {
            notifyDataSetChanged()
        }
    }

    inner class ConverterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val currencyFlag = view.currency_flag
        private val currencyCode = view.currency_code
        private val currencyName = view.currency_name
        private val currencyAmount = view.currency_amount
        private var amountTextWatcher: TextWatcher? = null

        init {
            setupCurrencyAmount()
            view.setOnClickListener {
                moveItemOnTop(layoutPosition)
                currencyAmount.requestFocus()
                currencyAmount.showKeyboard()
            }
        }

        private fun setupCurrencyAmount() {
            with(currencyAmount) {
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        moveItemOnTop(layoutPosition)
                    }
                    false
                }
                setOnEditorActionListener { v, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        v.clearFocus()
                        true
                    } else {
                        false
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        setSelection(length())
                        filters = arrayOf(DecimalInputFilter(10, 2))
                        amountTextWatcher = doOnTextChanged { text, _, _, _ ->
                            converterItemsListener.onBaseAmountChange(parseDouble(text))
                        }
                    } else {
                        removeTextChangedListener(amountTextWatcher)
                        filters = arrayOf()
                        if (adapterPosition == 0) {
                            hideKeyboard()
                        }
                    }
                }
            }
        }

        fun bind(item: ConverterItem) {
            if (!currencyAmount.isFocused) {
                currencyCode.text = item.currencyCode
                currencyName.text = item.currencyName
                currencyAmount.setText(item.formattedAmount)
                Glide.with(currencyFlag).load(item.flagUri).circleCrop().into(currencyFlag)
            }
        }
    }
}

interface ConverterItemsListener {
    fun onItemsChanged(items: List<ConverterItem>)
    fun onBaseAmountChange(amount: Double)
}