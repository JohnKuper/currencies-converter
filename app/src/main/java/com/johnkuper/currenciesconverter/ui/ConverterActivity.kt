package com.johnkuper.currenciesconverter.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.johnkuper.currenciesconverter.ConverterApplication
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.di.ViewModelFactory
import com.johnkuper.currenciesconverter.network.ConnectivityLiveData
import com.johnkuper.currenciesconverter.utils.createViewModel
import com.johnkuper.currenciesconverter.utils.updateMargins
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

class ConverterActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var connectivityLiveData: ConnectivityLiveData

    private lateinit var converterViewModel: ConverterViewModel
    private lateinit var converterAdapter: ConverterAdapter

    private val converterItemsListener = object : ConverterItemsListener {
        override fun onItemsChanged(items: List<ConverterItem>) {
            converterViewModel.onItemsChanged(items)
        }

        override fun onBaseAmountChange(amount: Double) {
            converterViewModel.onAmountChanged(amount)
        }
    }

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
        converter_list.adapter = ConverterAdapter(this, converterItemsListener).apply {
            setHasStableIds(true)
            converterAdapter = this
        }
    }
}