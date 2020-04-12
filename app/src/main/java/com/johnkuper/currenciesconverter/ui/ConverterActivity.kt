package com.johnkuper.currenciesconverter.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.johnkuper.currenciesconverter.ConverterApplication
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.di.ViewModelFactory
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.extensions.createViewModel
import com.johnkuper.currenciesconverter.extensions.onAnimationsFinished
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.converter_list_item.view.*
import javax.inject.Inject

class ConverterActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var converterViewModel: ConverterViewModel
    private lateinit var converterAdapter: ConverterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConverterApplication.appComponent.inject(this)

        converter_list.adapter = ConverterAdapter(this) {
            converterViewModel.onPrimaryItemChanged(it)
        }.also { converterAdapter = it }

        converterViewModel = createViewModel(viewModelFactory) {
            converterItems.observe(this@ConverterActivity, Observer {
                converterAdapter.setData(it)
            })
            startRatesPolling()
        }
    }
}

class ConverterAdapter(
    context: Context,
    private val onTopItemChanged: (ConverterItem) -> Unit
) : RecyclerView.Adapter<ConverterAdapter.ConverterViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private val items = mutableListOf<ConverterItem>()
    private val inflater = LayoutInflater.from(context)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConverterViewHolder {
        return ConverterViewHolder(inflater.inflate(R.layout.converter_list_item, parent, false)) {
            moveItemOnTop(it)
        }
    }

    private fun moveItemOnTop(fromPosition: Int) {
        val newTop = items[fromPosition]
        items.removeAt(fromPosition)
        items.add(0, newTop)
        notifyItemMoved(fromPosition, 0)
        recyclerView.scrollToPosition(0)
        recyclerView.onAnimationsFinished { onTopItemChanged(newTop) }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ConverterViewHolder, position: Int) {
        holder.bind(items[position])
    }

//    override fun getItemId(position: Int): Long {
//        return items[position].code.hashCode().toLong()
//    }

    // TODO Kuper optimize data updating
    fun setData(rates: List<ConverterItem>) {
        if (items.isEmpty()) {
            items.addAll(rates)
        } else {
            val newItems = items.map { converterItem ->
                rates.first { it.code == converterItem.code }
            }
            items.clear()
            items.addAll(newItems)
        }
        notifyDataSetChanged()
    }

    inner class ConverterViewHolder(view: View, onViewClicked: (Int) -> Unit) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                onViewClicked(layoutPosition)
            }
        }

        fun bind(item: ConverterItem) {
            itemView.currency_code.text = item.code
            itemView.currency_amount.setText(String.format("%.2f", item.amount))
        }
    }
}

