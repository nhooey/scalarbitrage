package com.hootionary.scalarbitrage.services

import com.hootionary.scalarbitrage.models.Types.CurrencyPriceMap
import scalaj.http.{Http, HttpResponse}
import spray.json._

object PriceService {

  import com.hootionary.scalarbitrage.json.JsonProtocol._

  val url = "https://fx.priceonomics.com/v1/rates/"

  def getPrices(): CurrencyPriceMap = {
    val response: HttpResponse[String] = Http(url).asString
    println("Got response, response: " + response)

    val json = response.body.parseJson
    println("Json: " + json)

    val currencyPriceMap = json.convertTo[CurrencyPriceMap]
    println("CurrencyPriceMap:")
    pprint.pprintln(currencyPriceMap)

    currencyPriceMap
  }
}
