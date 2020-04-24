package com.johnkuper.currenciesconverter.ui

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.johnkuper.currenciesconverter.ConverterApplication
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.di.ViewModelFactory
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.network.ConnectivityLiveData
import com.johnkuper.currenciesconverter.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.converter_list_item.view.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

class ConverterActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var connectivityLiveData: ConnectivityLiveData

    private lateinit var converterViewModel: ConverterViewModel
    private lateinit var converterAdapter: ConverterAdapter

    private val snackbar: Snackbar by lazy {
        Snackbar.make(root_view, getString(R.string.no_network_connection), Snackbar.LENGTH_INDEFINITE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConverterApplication.appComponent.inject(this)
        observeViewModel()
        observeNetworkState()
        initViews()
    }

    override fun onResume() {
        super.onResume()
//        kuperLog("ConverterActivity.onResume()")
        converterViewModel.startRatesPolling()
    }

    override fun onPause() {
        super.onPause()
        converterViewModel.stopRatesPolling()
    }

    private fun observeViewModel() {
        converterViewModel = createViewModel(viewModelFactory) {
            converterItemsLiveData.observe(this@ConverterActivity, Observer {
                progress_bar.hide()
                converterAdapter.setData(it)
            })
        }
    }

    private fun observeNetworkState() {
        connectivityLiveData.observe(this, Observer {
            if (it) {
                snackbar.dismiss()
                converter_list.updateMargins(bottomMargin = 0)
            } else {
                snackbar.show()
                converter_list.updateMargins(
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.snackbar_dodge_bottom_margin)
                )
            }
        })
    }

    private fun initViews() {
        toolbar_title.text = getString(R.string.rates_title)
        converter_list.adapter = ConverterAdapter(this, converterViewModel).apply {
            setHasStableIds(true)
            converterAdapter = this
        }
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

    //TODO Kuper refactor currencyAmount setup
    inner class ConverterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val currencyFlag = view.currency_flag
        private val currencyCode = view.currency_code
        private val currencyName = view.currency_name
        private val currencyAmount = view.currency_amount

        private var amountTextWatcher: TextWatcher? = null

        init {
            currencyAmount.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    moveItemOnTop(layoutPosition)
                }
                false
            }
            currencyAmount.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    v.clearFocus()
                    true
                } else {
                    false
                }
            }
            currencyAmount.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    kuperLog("onFocusChange(), hasFocus=$hasFocus, amount=${currencyAmount.text}, adapterPosition=$adapterPosition")
                    currencyAmount.setSelection(currencyAmount.length())
                    currencyAmount.filters = arrayOf(DecimalInputFilter(10, 2))
                    amountTextWatcher = currencyAmount.doOnTextChanged { text, _, _, _ ->
                        kuperLog("doOnTextChanged(), text=$text")
                        if (text.isNullOrEmpty()) {
                            converterViewModel.onAmountChanged(0.0)
                        } else {
                            converterViewModel.onAmountChanged(parseDouble(text))
                        }
                    }
                } else {
                    kuperLog("onFocusChange(), hasFocus=$hasFocus, amount=${currencyAmount.text}, adapterPosition=$adapterPosition")
                    currencyAmount.removeTextChangedListener(amountTextWatcher)
                    currencyAmount.filters = arrayOf()
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

        fun bind(item: ConverterItem) {
//            kuperLog("bind(), position = $position")
            if (!currencyAmount.isFocused) {
                currencyCode.text = item.currencyCode
                currencyName.text = item.currencyName
                currencyAmount.setText(item.formattedAmount)
                Glide.with(currencyFlag).load(item.flagUri).circleCrop().into(currencyFlag)
            }
        }
    }
}
