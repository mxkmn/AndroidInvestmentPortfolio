package com.example.investmentportfolio.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.example.investmentportfolio.storage.Portfolio
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@ExperimentalMaterial3Api
class PortfoliosList {
  @Preview(showBackground = true)
  @Composable
  private fun DefaultPreview() {
    val fakePortfoliosLive: LiveData<List<Portfolio>> by lazy { MutableLiveData(listOf(Portfolio("US stocks", 12345), Portfolio("RU stocks", 14600))) }
    val fakePortfolios = fakePortfoliosLive.observeAsState(emptyList())
    Window(fakePortfolios, false, {}, {}, {})
  }

  @Composable
  fun Window(portfolios: State<List<Portfolio>>, isRefreshing: Boolean, onRefresh: () -> Unit, onAddNewPortfolioClick: () -> Unit, onSomePortfolioClick: (Portfolio) -> Unit) {
    Scaffold(
      topBar = { CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.smallTopAppBarColors( containerColor = MaterialTheme.colorScheme.surfaceVariant )
      ) },
      floatingActionButton = { ExtendedFloatingActionButton(
        onClick = onAddNewPortfolioClick,
        icon = { Icon(Icons.Default.Add, null) },
        text = { Text(stringResource(R.string.add_portfolio)) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
      ) },
      content = { values ->
        SwipeRefresh(
          state = rememberSwipeRefreshState(isRefreshing),
          indicatorPadding = PaddingValues(top = values.calculateTopPadding()),
          onRefresh = onRefresh
        ) {
          Surface(
            modifier = Modifier.fillMaxSize().padding(values),
            color = MaterialTheme.colorScheme.surface
          ) {
            if (portfolios.value.isEmpty()) {
              CenteredText(stringResource(R.string.no_portfolios))
            } else {
              LazyColumn{
                items(portfolios.value.size) { i ->
                  val portfolio = portfolios.value[i]
                  PortfolioCard(portfolio, onSomePortfolioClick)
                }
              }
            }
          }
        }
      }
    )
  }

  @Composable
  private fun PortfolioCard(portfolio: Portfolio, onPortfolioClick: (Portfolio) -> Unit) {
    Card(
      modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
      ),
      shape = MaterialTheme.shapes.extraLarge,
      onClick = { onPortfolioClick(portfolio) }
    ) {
      Box(
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      ) {
        Text(
          text = portfolio.name,
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(end = 48.dp), // TODO: remove this hack
        )
//        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = getStrPrice(portfolio.priceInCents),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.align(Alignment.CenterEnd)
        )
      }
    }
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