package com.example.investmentportfolio.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.investmentportfolio.storage.Stock
import io.finnhub.api.apis.DefaultApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class PriceRefresher : ViewModel() { // viewmodel тут для возможностей viewModelScope.launch
  private val apiClient = DefaultApi()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> get() = _isRefreshing.asStateFlow()

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

  fun refresh() {
    if (_isRefreshing.value) { // выход, если уже обновляется (на всякий случай, сейчас нет необходимости в этой защите)
      return
    }

    thread {
      viewModelScope.launch {
        _isRefreshing.emit(true)
        delay(2000)
        _isRefreshing.emit(false)
      }
    }
  }
}