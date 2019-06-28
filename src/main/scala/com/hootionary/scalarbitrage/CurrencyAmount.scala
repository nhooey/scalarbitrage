package com.hootionary.scalarbitrage

final case class CurrencyAmount(currency: Currency, amount: Double) {

  def compare[T](operator: (Double, Double) => T,
                 other: CurrencyAmount): Option[T] = other.currency match {
    case Currency(currency.symbol) => Option(operator(other.amount, amount))
    case _                         => Option.empty
  }

  def ==(other: CurrencyAmount): Option[Boolean] =
    if (other.currency == currency) Option(other.amount == amount)
    else Option.empty

  def <(other: CurrencyAmount): Option[Boolean] =
    if (other.currency == currency) Option(other.amount < amount)
    else Option.empty

  def >(other: CurrencyAmount): Option[Boolean] =
    if (other.currency == currency) Option(other.amount > amount)
    else Option.empty

  def +(other: CurrencyAmount): Option[CurrencyAmount] =
    if (other.currency == currency)
      Option(CurrencyAmount(currency, other.amount + amount))
    else Option.empty
}
