package com.example.investmentportfolio.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.investmentportfolio.R
import com.example.investmentportfolio.logic.getStrPrice
import com.example.investmentportfolio.storage.Portfolio
import com.example.investmentportfolio.storage.PortfolioStockCrossRef
import com.example.investmentportfolio.storage.Stock
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@ExperimentalMaterial3Api
class PortfolioInfo {
  @Preview(showBackground = true)
  @Composable
  private fun DefaultPreview() {
    val fakePortfolio = Portfolio("Abracadabra", 123456)
    val fakePortfolioStockCrossRef = listOf(PortfolioStockCrossRef(1, 1, 3), PortfolioStockCrossRef(1, 2, 5))

    Window(fakePortfolio, fakePortfolioStockCrossRef, false,
      {},
      {},
      { },
      {},
      { portfolioStockCrossRef ->
        when (portfolioStockCrossRef) {
          fakePortfolioStockCrossRef[0] -> Stock("Apple", "AAPL", 1234, "US")
          fakePortfolioStockCrossRef[1] -> Stock("AMD", "AMD", 8901, "US")
          else -> Stock("Qualcomm", "QCOM", 5678, "US")
        }
      },
      {}
    )
  }

  @Composable
  fun Window(portfolio: Portfolio, portfolioStocks: List<PortfolioStockCrossRef>, isRefreshing: Boolean,
             onRefresh: () -> Unit,
             onSearchClick: () -> Unit,
             onRename: (Portfolio) -> Unit,
             onDeleteClick: () -> Unit,
             stockGetter: (PortfolioStockCrossRef) -> Stock?,
             onPortfolioStockClick: (PortfolioStockCrossRef) -> Unit
  ) {
    val isDialogOpen = remember { mutableStateOf(false)}

    Scaffold(
      topBar = { CenterAlignedTopAppBar(
        title = { Text(getName(portfolio)) },
        colors = TopAppBarDefaults.smallTopAppBarColors( containerColor = MaterialTheme.colorScheme.surfaceVariant )
      ) },
      floatingActionButton = { ExtendedFloatingActionButton(
        onClick = onSearchClick,
        icon = { Icon(Icons.Default.Search, null) },
        text = { Text(stringResource(R.string.stock_search)) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
      ) },
      content = { values ->
        SwipeRefresh(
          state = rememberSwipeRefreshState(isRefreshing),
          indicatorPadding = PaddingValues(top = values.calculateTopPadding()),
          onRefresh = onRefresh
        ) {
          Column(
            modifier = Modifier
              .padding(values)
              .fillMaxSize(),
          ) {
            ShowAlertDialog(isDialogOpen, portfolio, onRename)
            FlowRow(
              Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp).fillMaxWidth(),
              mainAxisSpacing = 8.dp,
              mainAxisAlignment = MainAxisAlignment.Center,
            ) {
              BeautifulAssistButton(stringResource(R.string.rename_portfolio), {isDialogOpen.value = true}/*onRenameClick*/, Icons.Outlined.Edit)
              BeautifulAssistButton(stringResource(R.string.delete_portfolio), onDeleteClick, Icons.Outlined.Delete)
            }
            if (portfolioStocks.isEmpty()) {
              CenteredText(stringResource(R.string.no_stocks))
            } else {
              LazyColumn {
                items(portfolioStocks.size) { i ->
                  val portfolioStock = portfolioStocks[i]
                  val stock = stockGetter(portfolioStock)
                  if (stock != null) {
//                    Text(text = stock.toString())
                    StockCard(portfolioStock, stock, onPortfolioStockClick)
                  }
                }
              }
            }
          }
        }
      }
    )
  }

  @Composable
  private fun StockCard(portfolioStock: PortfolioStockCrossRef, stock: Stock, onClick: (PortfolioStockCrossRef) -> Unit) {
    Card(
      modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
      ),
      shape = MaterialTheme.shapes.extraLarge
    ) {
      Row(
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column (
//          verticalArrangement = Arrangement.Center,
        ) {
          Text(
            text = stock.name,
            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier
//              .align(Alignment.Start)
//              .padding(end = 48.dp), // TODO: remove this hack
          )
          Text(
            text = "$${stock.ticker}",
            style = MaterialTheme.typography.titleSmall,
//            modifier = Modifier
//              .align(Alignment.Start)
//              .padding(end = 48.dp), // TODO: remove this hack
          )
        }
        Column (
//          Modifier.fillMaxHeight(),
//          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = getStrPrice(stock.priceInCents),
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = "Куплено ${portfolioStock.stocksInPortfolio}",
            style = MaterialTheme.typography.titleSmall,
          )
          Text(
            text = "Страна ${stock.country}",
            style = MaterialTheme.typography.titleSmall,
          )
        }
      }
    }
  }

  private fun getName(portfolio: Portfolio): String {
    return "${portfolio.name} | ${getStrPrice(portfolio.priceInCents)}"
  }

  @Composable
  private fun ShowAlertDialog(isDialogOpen: MutableState<Boolean>, portfolio: Portfolio, onRename: (Portfolio) -> Unit) {
    val nameInput = remember { mutableStateOf(portfolio.name) }

    if (isDialogOpen.value) {
      Dialog(onDismissRequest = { isDialogOpen.value = false }) {
        Surface(
          shape = MaterialTheme.shapes.large
        ) {
          Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text( text = stringResource(R.string.rename_portfolio) )

            Spacer(modifier = Modifier.padding(12.dp))

            OutlinedTextField(
              value = nameInput.value,
              onValueChange = { nameInput.value = it },
              trailingIcon = { IconButton(
                onClick = { nameInput.value = "" },
                content = { Icon(
                  imageVector = Icons.Outlined.Clear,
                  contentDescription = "Clear"
                ) }
              ) },
              label = { Text(text = stringResource(R.string.name)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Row(
              Modifier.padding(8.dp, 8.dp, 8.dp, 8.dp),
//              mainAxisSpacing = 16.dp,
//              mainAxisAlignment = MainAxisAlignment.Center,
            ) {
              BeautifulAssistButton(stringResource(R.string.cancel), {
                isDialogOpen.value = false
              })
              BeautifulAssistButton(stringResource(R.string.save_name), {
                portfolio.name = nameInput.value
                onRename(portfolio)
                isDialogOpen.value = false
              })
            }
          }
        }
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

  @Composable
  fun BeautifulButton(text: String, onClick: () -> Unit) {
    Button(
      onClick = onClick,
      shape = MaterialTheme.shapes.extraLarge,
//      contentPadding = PaddingValues(12.dp),
    ) {
      Text(text)
    }
  }

  @Composable
  fun BeautifulAssistButton(text: String, onClick: () -> Unit, image: ImageVector? = null) {
    AssistChip(
      modifier = Modifier.padding(8.dp),
      onClick = onClick,
      colors = AssistChipDefaults.assistChipColors(
        leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      leadingIcon = { if (image != null) Icon(
        imageVector = image,
        contentDescription = null
      ) },
      label = { Text(text) }
    )
  }
}