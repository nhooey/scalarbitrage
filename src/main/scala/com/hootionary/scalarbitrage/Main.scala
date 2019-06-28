package com.hootionary.scalarbitrage

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {

  type CurrencyPriceMap = Map[CurrencyPair, CurrencyPrice]

  def main(args: Array[String]): Unit = {

    println("Hello, World!")

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    import com.hootionary.scalarbitrage.json.JsonProtocol._

    val url = "https://fx.priceonomics.com/v1/rates/"
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = url))

    responseFuture.onComplete {
      case Success(res) =>
        res match {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
              println("Got response, body: " + body.utf8String)
              val json = body.utf8String.parseJson
              println("Json: " + json)
              val unmarshalled = json.convertTo[CurrencyPriceMap]
              println("CurrencyPriceMap: " + unmarshalled)
            }
        }

      case Failure(_) => sys.error(s"Failed to make request to URL: ${url}")
    }

    println("Should exit now...")
  }
}
