package com.example.investmentportfolio.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.investmentportfolio.storage.Stock
import io.finnhub.api.apis.DefaultApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.math.min

class PriceRefresher : ViewModel() { // viewmodel тут для возможностей viewModelScope.launch
  private val apiClient = DefaultApi()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> get() = _isRefreshing.asStateFlow()

  private var parsedStocks = mutableListOf<Stock>()
  private var remainingStocks = 0
  
  private val _newParsedStock = MutableSharedFlow<Stock?>(extraBufferCapacity = 50)
  private val newParsedStock: SharedFlow<Stock?> get() = _newParsedStock.asSharedFlow()

  private val _updatedStocks = MutableStateFlow<List<Stock>>(emptyList())
  val updatedStocks: StateFlow<List<Stock>> get() = _updatedStocks.asStateFlow()
  
  init {
    viewModelScope.launch {
      newParsedStock.collect { parsedStock ->
        if (parsedStock != null) {
          parsedStocks.add(parsedStock)
        }

        remainingStocks--

        if (remainingStocks == 0) {
          _updatedStocks.value = parsedStocks
          viewModelScope.launch {
            _isRefreshing.emit(false)
          }
        }
      }
    }
  }

  private fun setNewStocksList(max: Int) {
    parsedStocks = mutableListOf() // чистка предыдущего списка
    remainingStocks = max
  }
  
  fun addNewStock(stock: Stock, adder: (Stock) -> Unit) {
    thread {
      viewModelScope.launch {
        _isRefreshing.value = true
      }

      try {
        val response = apiClient.companyProfile2(stock.ticker, null, null)
        stock.country = response.country ?: "Unknown"
      } catch (e: Exception) {
        stock.country = "Unknown"
      }
      adder(stock)

      viewModelScope.launch {
        _isRefreshing.value = false
      }
    }
  }

  fun refresh(stocks: List<Stock>) {
    if (_isRefreshing.value) { // выход, если уже обновляется (на всякий случай, сейчас нет необходимости в этой защите)
      return
    }

    thread {
      viewModelScope.launch {
        _isRefreshing.emit(true)
      }

      val max = min(stocks.size-1, 45)
      setNewStocksList(max)

      for (i in 0 until max) {
        thread {
          val stock = stocks[i]
          try {
            val price = apiClient.quote(stock.ticker).c!!
            if (price > 0) {
              stock.priceInCents = (price*100).toLong()
              viewModelScope.launch {
                _newParsedStock.emit(stock)
              }
            }
            else viewModelScope.launch {
              _newParsedStock.emit(null)
            }
          } catch (e: Exception) { viewModelScope.launch { // если что-то null или ошибка получения данных
            _newParsedStock.emit(null)
          } }
        }
      }
    }
  }
}