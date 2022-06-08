package com.example.investmentportfolio.ui

sealed class NavRoute(val path: String) { // https://vtsen.hashnode.dev/simple-jetpack-compose-navigation-example#heading-too-much-boilerplate-code
  object PortfoliosList: NavRoute("PortfoliosList")
  object PortfolioInfo: NavRoute("PortfolioInfo") {
    const val portfolioId = "portfolioId"
  }
  object Search: NavRoute("Search") {
    const val portfolioId = "portfolioId"
  }

  fun withArgs(vararg args: String): String { // build navigation path (for screen navigation)
    return buildString {
      append(path)
      args.forEach{ arg ->
        append("/$arg")
      }
    }
  }

  fun withArgsFormat(vararg args: String) : String { // build and setup route format (in navigation graph)
    return buildString {
      append(path)
      args.forEach{ arg ->
        append("/{$arg}")
      }
    }
  }
}