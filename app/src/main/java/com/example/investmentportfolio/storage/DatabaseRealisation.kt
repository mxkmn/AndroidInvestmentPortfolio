package com.example.investmentportfolio.storage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

interface BaseDao<T> {
  @Insert
  fun insert(entry: T)
  @Insert
  fun insert(entries: List<T>)

  @Update
  fun update(entry: T)
  @Update
  fun update(entries: List<T>)

  @Delete
  fun delete(entry: T)
  @Delete
  fun delete(entries: List<T>)
}

@Database(entities = [Stock::class, Portfolio::class, PortfolioStockCrossRef::class], version = 1)
private abstract class DatabaseRealisation : RoomDatabase() {
  abstract fun portfolio(): PortfolioDao
  abstract fun stock(): StockDao
  abstract fun portfolioToStock(): PortfolioStockCrossRefDao
}

class DatabaseWorker(context: Context) : ViewModel() {
  private lateinit var db: DatabaseRealisation

  private val _isInitialized = MutableStateFlow<Boolean?>(null)
  val isInitialized: StateFlow<Boolean?> get() = _isInitialized.asStateFlow()

  init {
    try {
      db = Room.databaseBuilder(context, DatabaseRealisation::class.java, "portfolios_and_stocks.db").allowMainThreadQueries().build()
      viewModelScope.launch {
        _isInitialized.emit(true)
      }
    } catch (e: Exception) {
      println("Database initialization error: $e")
      viewModelScope.launch {
        _isInitialized.emit(false)
      }
    }
  }


  //  fun addPortfolio(portfolio: Portfolio) = db.portfolio().insert(portfolio)
//  fun getPortfolios() = db.portfolio().getAll()
//  fun getPortfolioStocks() = db.portfolioToStock().getAll()

  fun getStocks() = db.stock().getAll()
  fun updateStocks(stocks: List<Stock>) = db.stock().update(stocks)
  fun getStocksInPortfolio(portfolioId: Int) = db.portfolioToStock().getAllByPortfolioId(portfolioId)
  fun getStockById(id: Int): Stock? {
    val stocks = db.stock().getById(id)
    return if (stocks.isEmpty()) null else stocks[0]
  }
  fun updatePortfolio(portfolio: Portfolio) = db.portfolio().update(portfolio)
  fun updatePortfolioStock(portfolioStock: PortfolioStockCrossRef) = db.portfolioToStock().update(portfolioStock)
  fun deletePortfolioStock(portfolioStock: PortfolioStockCrossRef) = db.portfolioToStock().delete(portfolioStock)

  val portfoliosLive = db.portfolio().fetchAll()

  fun getPortfolioById(id: Int) = db.portfolio().getById(id)


  fun addNewDefaultPortfolio() {
    thread {
      db.portfolio().getAll().size.let {
        val number = it + 1
        val newPortfolio = Portfolio( "Портфель $number", 0)
        db.portfolio().insert(newPortfolio)
      }
    }
  }

  fun addStockToPortfolio(stock: Stock, portfolioId: Int) {
    thread {
      val stocksInDb = db.stock().getByTicker(stock.ticker)
      if (stocksInDb.isEmpty()) {
        db.stock().insert(stock)
        val stocksInDb2 = db.stock().getByTicker(stock.ticker)
        db.portfolioToStock().insert(PortfolioStockCrossRef(portfolioId, stocksInDb2[0].id, 1))
      }
      else {
        val portfolioStockEntry = db.portfolioToStock().getByPortfolioIdAndStockId(portfolioId, stocksInDb[0].id)
        if (portfolioStockEntry.isEmpty()) {
          db.portfolioToStock().insert(PortfolioStockCrossRef(portfolioId, stocksInDb[0].id, 1))
        }
        else {
          db.portfolioToStock().update(PortfolioStockCrossRef(portfolioId, stocksInDb[0].id, portfolioStockEntry[0].stocksInPortfolio + 1))
        }
      }
    }
  }

  fun deletePortfolio(portfolio: Portfolio) {
    val stockUsages = db.portfolioToStock().getAllByPortfolioId(portfolio.id)
    db.portfolioToStock().delete(stockUsages) // удаление всех связей портфеля с акциями

    db.portfolio().delete(portfolio) // удаление портфеля
  }

  fun deleteUnusedStocks() {
    val stocks = db.stock().getAll()
    val stockUsages = db.portfolioToStock().getAll()

    val unusedStocks = stocks.filter { stock ->
      stockUsages.none {
        it.stockId == stock.id
      }
    }
    db.stock().delete(unusedStocks)
  }
}