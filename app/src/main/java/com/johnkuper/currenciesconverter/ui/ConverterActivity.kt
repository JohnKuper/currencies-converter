package com.johnkuper.currenciesconverter.ui

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.johnkuper.currenciesconverter.ConverterApplication
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.di.ViewModelFactory
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.extensions.createViewModel
import com.johnkuper.currenciesconverter.extensions.hideKeyboard
import com.johnkuper.currenciesconverter.extensions.onAnimationsFinished
import com.johnkuper.currenciesconverter.extensions.showKeyboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.converter_list_item.view.*
import java.lang.Double.parseDouble
import java.util.*
import javax.inject.Inject

class ConverterActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var converterViewModel: ConverterViewModel
    private lateinit var converterAdapter: ConverterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConverterApplication.appComponent.inject(this)

        converterViewModel = createViewModel(viewModelFactory) {
            converterItemsLiveData.observe(this@ConverterActivity, Observer {
                converterAdapter.setData(it)
            })
        }

        converter_list.adapter = ConverterAdapter(this, converterViewModel).apply {
            setHasStableIds(true)
            converterAdapter = this
        }
    }

    override fun onResume() {
        super.onResume()
        converterViewModel.startRatesUpdates()
    }

    override fun onPause() {
        super.onPause()
        converterViewModel.stopRatesUpdates()
    }
}

class ConverterAdapter(
    context: Context,
    // TODO Kuper is it good to pass it here?
    private val converterViewModel: ConverterViewModel
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
//        kuperLog("onBindViewHolder(), position = $position")
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return items[position].code.hashCode().toLong()
    }

    private fun moveItemOnTop(fromPosition: Int) {
        if (fromPosition > 0) {
            val newTop = items[fromPosition]
            items.removeAt(fromPosition)
            items.add(0, newTop)
            notifyItemMoved(fromPosition, 0)
            recyclerView.scrollToPosition(0)
            converterViewModel.onItemsChanged(items)
        }
    }

    fun setData(newItems: List<ConverterItem>) {
        items.clear()
        items.addAll(newItems)
        recyclerView.onAnimationsFinished {
            notifyDataSetChanged()
        }
    }

    // TODO Kuper enter should remove focus and close the keyboard
    inner class ConverterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val currencyAmount = view.currency_amount
        private val currencyCode = view.currency_code

        private var amountTextWatcher: TextWatcher? = null

        init {
            currencyAmount.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    kuperLog("onFocusChange(), hasFocus=$hasFocus, amount=${currencyAmount.text}, adapterPosition=$adapterPosition")
                    currencyAmount.setSelection(currencyAmount.length())
                    amountTextWatcher = currencyAmount.doOnTextChanged { text, _, _, _ ->
                        kuperLog("doOnTextChanged(), text=$text")
                        // TODO Kuper parse double with comma
                        if (text.isNullOrEmpty()) {
                            converterViewModel.onAmountChanged(0.0)
                        } else {
                            converterViewModel.onAmountChanged(parseDouble(text.toString()))
                        }
                    }
                } else {
                    kuperLog("onFocusChange(), hasFocus=$hasFocus, amount=${currencyAmount.text}, adapterPosition=$adapterPosition")
                    currencyAmount.removeTextChangedListener(amountTextWatcher)
                    if (adapterPosition == 0) {
                        currencyAmount.hideKeyboard()
                    }
                }
            }
            view.setOnClickListener {
                moveItemOnTop(layoutPosition)
                currencyAmount.requestFocus()
                currencyAmount.showKeyboard()
            }
        }

        fun bind(item: ConverterItem, position: Int) {
//            kuperLog("bind(), position = $position")
            if (!currencyAmount.isFocused) {
                currencyCode.text = item.code
                currencyAmount.setText(String.format(Locale.US, "%.2f", item.amount))
            }
        }
    }
}
