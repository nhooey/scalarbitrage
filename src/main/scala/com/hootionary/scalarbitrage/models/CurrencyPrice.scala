package com.hootionary.scalarbitrage.models

final case class CurrencyPrice(finance: Currency,
                               settle: Currency,
                               price: Double)
