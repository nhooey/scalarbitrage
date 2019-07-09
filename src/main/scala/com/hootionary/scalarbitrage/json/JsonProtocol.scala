package com.hootionary.scalarbitrage.json

import com.hootionary.scalarbitrage.models.{CurrencyPair, CurrencyPrice}
import com.hootionary.scalarbitrage.models.Types.CurrencyPriceMap
import spray.json.{
  DefaultJsonProtocol,
  JsNumber,
  JsObject,
  JsValue,
  RootJsonFormat
}

object JsonProtocol extends DefaultJsonProtocol {

  implicit object CurrencyPriceMapFormat
      extends RootJsonFormat[CurrencyPriceMap] {

    def write(map: CurrencyPriceMap) =
      new JsObject(for ((k, v) <- map) yield {
        s"${k.base}_${k.counter}" -> new JsNumber(v.price)
      })

    def read(value: JsValue): CurrencyPriceMap =
      for ((k, v) <- value.asJsObject.fields) yield {
        val pair = CurrencyPair(k)
        pair -> CurrencyPrice(pair.base,
                              pair.counter,
                              v.convertTo[String].toDouble)
      }
  }

}
