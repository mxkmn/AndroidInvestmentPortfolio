package com.example.investmentportfolio.storage

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "stocks")
data class Stock(
  val name: String = "",
  val ticker: String = "",
  var priceInCents: Long = 0,
  var country: String = "",

  @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface StockDao : BaseDao<Stock> {
  @Query("SELECT * FROM stocks")
  fun getAll(): List<Stock>

  @Query("SELECT * FROM stocks WHERE id = :id")
  fun getById(id: Int): List<Stock>

  @Query("SELECT * FROM stocks WHERE ticker = :ticker")
  fun getByTicker(ticker: String): List<Stock>
}