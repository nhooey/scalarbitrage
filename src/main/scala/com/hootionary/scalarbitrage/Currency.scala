package com.hootionary.scalarbitrage

case class Currency(symbol: String) {

  def ==(currency: Currency): Boolean = symbol == currency.symbol
}
