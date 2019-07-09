package com.hootionary.scalarbitrage.models

case class Currency(symbol: String) {

  def ==(currency: Currency): Boolean = symbol == currency.symbol
}
