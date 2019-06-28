package com.hootionary.scalarbitrage

final case class CurrencyPrice(finance: Currency,
                               settle: Currency,
                               price: Double)
