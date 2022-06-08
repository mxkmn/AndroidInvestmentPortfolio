package com.example.investmentportfolio.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.investmentportfolio.storage.Stock
import io.finnhub.api.apis.DefaultApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.math.min

class StockSearcher : ViewModel() { // viewmodel тут для возможностей viewModelScope.launch
  private val apiClient = DefaultApi()

  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> get() = _isSearching.asStateFlow()

  private var parsedStocks = mutableListOf<Stock>()
  private var remainingStocks = 0

  private val _newParsedStock = MutableSharedFlow<Stock?>(extraBufferCapacity = 50)
  private val newParsedStock: SharedFlow<Stock?> get() = _newParsedStock.asSharedFlow()

  private val _foundStocks = MutableStateFlow<List<Stock>>(emptyList())
  val foundStocks: StateFlow<List<Stock>> get() = _foundStocks.asStateFlow()

  init {
    viewModelScope.launch {
      newParsedStock.collect { parsedStock ->
        if (parsedStock != null) {
          parsedStocks.add(parsedStock)
        }

        remainingStocks--

        if (remainingStocks == 0) {
          _foundStocks.value = parsedStocks
          viewModelScope.launch {
            _isSearching.emit(false)
          }
        }
      }
    }
  }

  private fun setNewStocksList(max: Int) {
    parsedStocks = mutableListOf() // чистка предыдущего списка
    remainingStocks = max
  }

  fun searchStocksByString(searchQuery: String) {
    if (_isSearching.value) { // выход, если уже обновляется
      return
    }
    viewModelScope.launch {
      _isSearching.emit(true)
    }

    thread {
      val notParsedStock = apiClient.symbolSearch(searchQuery).result ?: listOf() // API symbol lookup
      val max = min(notParsedStock.size-1, 45)
      setNewStocksList(max)

      for (i in 0 until max) {
        thread {
          val stock = notParsedStock[i]
          try {
            val price = apiClient.quote(stock.displaySymbol!!).c!!
            if (price > 0) viewModelScope.launch {
              _newParsedStock.emit(Stock(stock.description!!, stock.symbol!!, (price*100).toLong()))
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