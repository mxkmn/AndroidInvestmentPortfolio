package com.example.investmentportfolio.logic

fun getStrPrice(price: Long): String { // TODO: do this better...
  var priceAsString = price.toString()
  return when (val priceLength = priceAsString.length) {
    1 -> "$.0$price"
    2 -> "$.$price"
    3 -> "$${priceAsString.substring(0, 1)}.${priceAsString.substring(1, 3)}"
    4 -> "$${priceAsString.substring(0, 2)}.${priceAsString.substring(2, 4)}"
    else -> {
      val outPriceStr = StringBuilder("$")

      val firstChars = (priceLength-2) % 3
      if (firstChars != 0) {
        outPriceStr.append("${priceAsString.substring(0, firstChars)} ")
        priceAsString = priceAsString.substring(firstChars)
      }

      while (priceAsString.length != 5) {
        outPriceStr.append("${priceAsString.substring(0, 3)} ")
        priceAsString = priceAsString.substring(3)
      }
      outPriceStr.append("${priceAsString.substring(0, 3)}.${priceAsString.substring(3)}")

      outPriceStr.toString()
    }
  }
}