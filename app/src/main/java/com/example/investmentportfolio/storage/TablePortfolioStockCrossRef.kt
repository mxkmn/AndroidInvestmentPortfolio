package com.example.investmentportfolio.storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "portfolios_stocks"/*, primaryKeys = ["portfolioId", "portfolioId"]*/)
data class PortfolioStockCrossRef(
  var portfolioId: Int,
  var stockId: Int,
  var stocksInPortfolio: Int,

  @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Dao
interface PortfolioStockCrossRefDao : BaseDao<PortfolioStockCrossRef> {
  @Query("SELECT * FROM portfolios_stocks")
  fun getAll(): List<PortfolioStockCrossRef>

  @Query("SELECT * FROM portfolios_stocks WHERE portfolioId = :portfolioId")
  fun getAllByPortfolioId(portfolioId: Int): List<PortfolioStockCrossRef>

  @Query("SELECT * FROM portfolios_stocks WHERE portfolioId = :portfolioId")
  fun fetchAllByPortfolioId(portfolioId: Int): LiveData<List<PortfolioStockCrossRef>>

  @Query("SELECT * FROM portfolios_stocks WHERE portfolioId = :portfolioId & stockId = :stockId")
  fun getByPortfolioIdAndStockId(portfolioId: Int, stockId: Int): List<PortfolioStockCrossRef>
}

//data class PortfolioWithCompanies(
//  @Embedded val portfolio: Portfolio,
//  @Relation(
//    parentColumn = "portfolioId",
//    entityColumn = "companyId",
//    associateBy = Junction(PortfolioCompanyCrossRef::class)
//  )
//  val companies: List<Company>
//)
//
//data class SongWithPlaylists(
//  @Embedded val song: Song,
//  @Relation(
//    parentColumn = "songId",
//    entityColumn = "playlistId",
//    associateBy = Junction(PlaylistSongCrossRef::class)
//  )
//  val playlists: List<Playlist>
//)