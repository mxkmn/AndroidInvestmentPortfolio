package com.example.investmentportfolio.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.investmentportfolio.R
import com.example.investmentportfolio.logic.getStrPrice
import com.example.investmentportfolio.storage.Stock


@ExperimentalMaterial3Api
class Search {
  @Preview(showBackground = true)
  @Composable
  private fun DefaultPreview() {
    val fakeStocksLive: LiveData<List<Stock>> by lazy { MutableLiveData(listOf(Stock("Apple", "AAPL", 1234, "US"), Stock("Google", "GOOG", 1234, "US"), Stock("Microsoft", "MSFT", 1234, "US"))) }
    val fakeStocks = fakeStocksLive.observeAsState(emptyList())

    val fakeIsSearchingLive: LiveData<Boolean> by lazy { MutableLiveData(false) }
    val fakeIsSearching = fakeIsSearchingLive.observeAsState(false)

    Search().Window(fakeStocks, fakeIsSearching, {}, {})
  }

  @Composable
  fun Window(stocks: State<List<Stock>>, isSearchinggg: State<Boolean>, onSearch: (String) -> Unit, onStockClick: (Stock) -> Unit) {
    val isSearching by isSearchinggg
    val input = remember { mutableStateOf("") }

    Scaffold(
      topBar = { CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.stock_search)) },
        colors = TopAppBarDefaults.smallTopAppBarColors( containerColor = MaterialTheme.colorScheme.surfaceVariant )
      ) },
      content = { values ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(values)
        ) {
          Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = input.value,
              onValueChange = { input.value = it },
              trailingIcon = { IconButton(
                onClick = { input.value = "" },
                content = { Icon(
                  imageVector = Icons.Outlined.Clear,
                  contentDescription = "Clear"
                ) }
              ) },
              label = { Text(text = stringResource(R.string.search_suggestion)) },
              singleLine = true
            )
            if (!isSearching) {
              IconButton(
//              modifier = Modifier.clickable(enabled = !isSearching, onClick = { }),
//              colors = ColorScheme.Light.onSurface,
                onClick = { onSearch(input.value) },
                content = { Icon(
                  imageVector = Icons.Outlined.Search,
                  contentDescription = "Search"
                ) }
              )
            }
          }

          if (stocks.value.isEmpty()) {
            CenteredText(stringResource(R.string.no_stocks))
          } else {
            LazyColumn {
              items(stocks.value.size) { i ->
                val stock = stocks.value[i]
                StockCard(stock, onStockClick)
              }
            }
          }
        }
      }
    )
  }

  @Composable
  private fun StockCard(stock: Stock, onStockClick: (Stock) -> Unit) {
    Card(
      modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
      ),
      shape = MaterialTheme.shapes.extraLarge,
      onClick = { onStockClick(stock) }
    ) {
      Column(
        Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = "${stock.name} | $${stock.ticker}",
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          text = getStrPrice(stock.priceInCents),
          style = MaterialTheme.typography.titleMedium
        )
      }
//      Row(
//        modifier = Modifier
//          .padding(16.dp)
//          .fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//      ) {
//        Column (
//          verticalArrangement = Arrangement.Center,
//        ) {
//          Text(
//            text = stock.name,
//            style = MaterialTheme.typography.titleLarge,
////            modifier = Modifier
////              .align(Alignment.Start)
////              .padding(end = 48.dp), // TODO: remove this hack
//          )
//          Text(
//            text = "$${stock.ticker}",
//            style = MaterialTheme.typography.titleSmall,
////            modifier = Modifier
////              .align(Alignment.Start)
////              .padding(end = 48.dp), // TODO: remove this hack
//          )
//        }
//        Column (
////          Modifier.fillMaxHeight(),
////          verticalArrangement = Arrangement.Center
//        ) {
//          Text(
//            text = getStrPrice(stock.priceInCents),
//            style = MaterialTheme.typography.titleMedium,
//          )
//        }
      }
//      Box(
//        modifier = Modifier
//          .padding(16.dp, 0.dp, 16.dp, 16.dp)
//          .fillMaxWidth(),
//      ) {
//        Text(
//          text = "$${stock.ticker}",
//          style = MaterialTheme.typography.titleSmall,
//          modifier = Modifier
//            .align(Alignment.CenterStart)
//            .padding(end = 48.dp), // TODO: remove this hack
//        )
//      }
//    }
  }

  @Composable
  fun CenteredText(text: String) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(text, Modifier.padding(16.dp))
    }
  }
}