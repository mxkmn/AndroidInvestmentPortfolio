package com.example.investmentportfolio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.investmentportfolio.logic.PriceRefresher
import com.example.investmentportfolio.logic.StockSearcher
import com.example.investmentportfolio.storage.DatabaseWorker
import com.example.investmentportfolio.storage.Portfolio
import com.example.investmentportfolio.storage.PortfolioStockCrossRef
import com.example.investmentportfolio.storage.Stock
import com.example.investmentportfolio.ui.NavRoute
import com.example.investmentportfolio.ui.layouts.PortfolioInfo
import com.example.investmentportfolio.ui.layouts.PortfoliosList
import com.example.investmentportfolio.ui.layouts.Search
import com.example.investmentportfolio.ui.theme.InvestmentPortfolioTheme
import io.finnhub.api.apis.DefaultApi
import io.finnhub.api.infrastructure.ApiClient
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
  private var apiClient: DefaultApi
  init {
    ApiClient.apiKey["token"] = "caea8aaad3i9ra0qqu90"
    apiClient = DefaultApi()
  }

  private val refreshPrices = PriceRefresher()
  private lateinit var db: DatabaseWorker

  override fun onDestroy() { // после закрытия активити очищаем некупленные компании из базы
    db.deleteUnusedStocks()
    super.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    db = DatabaseWorker(this)

    lifecycleScope.launch {
      db.isInitialized.collect {
        if (it == true) { // инициализация UI и прочего после подключения к БД
          setContent {
            InvestmentPortfolioTheme {
              val navController = rememberNavController()
              BuildNavGraph(navController)
            }
          }
          lifecycleScope.launch {
            refreshPrices.updatedStocks.collect { stocks ->
              db.updateStocks(stocks)
            }
          }

          refreshPrices.refresh(db.getStocks()) // обновление цен акций и портфелей
        }
        else if (it == false) {
          Toast.makeText(applicationContext, getText(R.string.db_error), Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  @Composable
  private fun BuildNavGraph(navController: NavHostController) {
    NavHost(
      navController = navController,
      startDestination = NavRoute.PortfoliosList.path
    ) {
      composable(route = NavRoute.PortfoliosList.path) { // главный экран  ////////////////////////////////////////////////////////////
        val portfolios = db.portfoliosLive.observeAsState(emptyList())
        val isRefreshing by refreshPrices.isRefreshing.collectAsState()

        PortfoliosList().Window(portfolios, isRefreshing,
          onRefresh = {
            refreshPrices.refresh(db.getStocks())
          },
          onAddNewPortfolioClick = { // при клике на кнопку снизу добавляем новый портфель
            db.addNewDefaultPortfolio()
          },
          onSomePortfolioClick = { portfolio: Portfolio -> // при клике на портфель перейти на его страницу
            navController.navigate(NavRoute.PortfolioInfo.withArgs(portfolio.id.toString()))
          }
        )
      }

      composable(route = NavRoute.PortfolioInfo.withArgsFormat(NavRoute.PortfolioInfo.portfolioId), // страница портфеля //////////////
        arguments = listOf(
          navArgument(NavRoute.PortfolioInfo.portfolioId) {
            type = NavType.IntType
//          nullable = true
          }
        ) ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val portfolioId = args?.getInt(NavRoute.PortfolioInfo.portfolioId) ?: -1
        val portfolio = db.getPortfolioById(portfolioId).firstOrNull() ?: return@composable

        val isRefreshing by refreshPrices.isRefreshing.collectAsState()

        val stocks = db.getStocksInPortfolio(portfolioId)

        var portfolioPrice: Long = 0
        for (stock in stocks) {
          val price = db.getStockById(stock.stockId)?.priceInCents ?: 0
          portfolioPrice += price * stock.stocksInPortfolio
        }
        portfolio.priceInCents = portfolioPrice
        db.updatePortfolio(portfolio)


        PortfolioInfo().Window(portfolio, stocks, isRefreshing,
          onRefresh = {
            refreshPrices.refresh(db.getStocks())
         },
          onSearchClick = {
            navController.navigate(NavRoute.Search.withArgs(portfolioId.toString()))
          },
          onRename = {
            db.updatePortfolio(portfolio)
            navController.popBackStack()
            navController.navigate(NavRoute.PortfolioInfo.withArgs(portfolioId.toString()))
          },
          onDeleteClick = {
            db.deletePortfolio(portfolio)
            navController.popBackStack()
          },
          stockGetter = { portfolioStockCrossRef: PortfolioStockCrossRef ->
            db.getStockById(portfolioStockCrossRef.stockId)
          },
          onPortfolioStockChange = { portfolioStockCrossRef: PortfolioStockCrossRef, num: Int ->
            portfolioStockCrossRef.stocksInPortfolio += num
            if (portfolioStockCrossRef.stocksInPortfolio == 0) {
              db.deletePortfolioStock(portfolioStockCrossRef)
            }
            else {
              db.updatePortfolioStock(portfolioStockCrossRef)
            }
            navController.popBackStack()
            navController.navigate(NavRoute.PortfolioInfo.withArgs(portfolioId.toString()))
          }
        )
      }





      composable(route = NavRoute.Search.withArgsFormat(NavRoute.Search.portfolioId), arguments = listOf(
        navArgument(NavRoute.Search.portfolioId) {
          type = NavType.IntType
//          nullable = true
        } )
      ) { navBackStackEntry -> // страница поиска /////////////////////////////////////////////////////////////////////////////////////
        val args = navBackStackEntry.arguments
        val portfolioId = args?.getInt(NavRoute.PortfolioInfo.portfolioId) ?: -1
        val stockSearcher = StockSearcher()
//        val isSearching by stockSearcher.isSearching.collectAsState()

//        val stocks = listOf(Stock("Apple", "AAPL", 1234, "US"), Stock("Google", "GOOG", 1234, "US"), Stock("AMD", "AMD", 8901, "US"), Stock("Microsoft", "MSFT", 1234, "US"))

        val stocks = stockSearcher.foundStocks.collectAsState()
        Search().Window(stocks, stockSearcher.isSearching.collectAsState(),
          onSearch = { searchQuery: String ->
            stockSearcher.searchStocksByString(searchQuery)
          },
          onStockClick = { stock: Stock ->
            refreshPrices.addNewStock(stock, { extendedStock ->
              db.addStockToPortfolio(extendedStock, portfolioId)
            })
            navController.popBackStack()
          }
        )
      }
    }
  }
}