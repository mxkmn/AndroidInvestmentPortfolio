package com.example.investmentportfolio.storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "portfolios")
data class Portfolio(
  var name: String = "",
  var priceInCents: Long = 0,

  @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface PortfolioDao : BaseDao<Portfolio> {
  @Query("SELECT * FROM portfolios")
  fun getAll(): List<Portfolio>

  @Query("SELECT * FROM portfolios")
  fun fetchAll(): LiveData<List<Portfolio>>

  @Query("SELECT * FROM portfolios ORDER BY name")
  fun getAllSorted(): List<Portfolio>

  @Query("SELECT * FROM portfolios WHERE id = :id")
  fun getById(id: Int): List<Portfolio>

//  @Query("SELECT * FROM lesson_table WHERE internalWeek < :internalCurrentWeek")
//  fun getOutdatedLessons(internalCurrentWeek: Int): List<Lesson>
//

//
//  @Query("SELECT * FROM lesson_table WHERE internalWeek = :internalWeek")
//  fun getByInternalWeek(internalWeek: Int): List<Lesson>
//
//  @Query("SELECT * FROM lesson_table WHERE internalWeek = :internalWeek ORDER BY dayOfWeek, time, subgroup")
//  fun getByInternalWeekSorted(internalWeek: Int): List<Lesson>
}