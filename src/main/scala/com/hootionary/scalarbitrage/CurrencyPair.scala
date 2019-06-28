package com.hootionary.scalarbitrage

final case class CurrencyPair(base: Currency, counter: Currency) {

  def ==(other: CurrencyPair): Boolean =
    (base == other.base && counter == other.counter) || (base == other.counter && counter == other.base)
}

object CurrencyPair {

  def apply(base: String, counter: String): CurrencyPair = {
    this(Currency(base), Currency(counter))
  }

  def apply(tokens: Array[String]): CurrencyPair = {
    this(tokens(0), tokens(1))
  }

  def apply(pair: String): CurrencyPair = {
    this(pair.split("_"))
  }

}
