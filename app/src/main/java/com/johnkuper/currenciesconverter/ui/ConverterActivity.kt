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
            converterViewModel.onItemsChanged(it)
        }.also { converterAdapter = it }
        converter_list.setHasFixedSize(true)

        converterViewModel = createViewModel(viewModelFactory) {
            converterItemsLiveData.observe(this@ConverterActivity, Observer {
                converterAdapter.setData(it)
            })
            startRatesPolling()
        }
    }
}

class ConverterAdapter(
    context: Context,
    private val onItemsChanged: (List<ConverterItem>) -> Unit
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

    override fun onBindViewHolder(holder: ConverterViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    private fun moveItemOnTop(fromPosition: Int) {
        val newTop = items[fromPosition]
        items.removeAt(fromPosition)
        items.add(0, newTop)
        onItemsChanged(items)
        notifyItemMoved(fromPosition, 0)
        recyclerView.scrollToPosition(0)
    }

    fun setData(newItems: List<ConverterItem>) {
        if (!recyclerView.isAnimating) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
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

//class ConverterDiffUtil(
//    private val oldList: List<ConverterItem>,
//    private val newList: List<ConverterItem>
//) : DiffUtil.Callback() {
//
//    override fun getOldListSize() = oldList.size
//
//    override fun getNewListSize() = newList.size
//
//    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        return oldList[oldItemPosition].code == newList[newItemPosition].code
//    }
//
//    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        return oldList[oldItemPosition] == newList[newItemPosition]
//    }
//}

