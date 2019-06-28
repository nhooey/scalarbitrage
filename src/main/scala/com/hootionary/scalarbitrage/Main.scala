package com.hootionary.scalarbitrage

import scalaj.http.{Http, HttpResponse}
import spray.json._

object Main {

  type CurrencyPriceMap = Map[CurrencyPair, CurrencyPrice]

  def main(args: Array[String]): Unit = {

    println("Hello, World!")

    import com.hootionary.scalarbitrage.json.JsonProtocol._

    val url = "https://fx.priceonomics.com/v1/rates/"

    val response: HttpResponse[String] = Http(url).asString

    println("Got response, response: " + response)
    val json = response.body.parseJson
    println("Json: " + json)
    val unmarshalled = json.convertTo[CurrencyPriceMap]
    println("CurrencyPriceMap: " + unmarshalled)
  }
}
