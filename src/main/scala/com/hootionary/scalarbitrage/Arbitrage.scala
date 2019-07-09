package com.hootionary.scalarbitrage

import com.hootionary.scalarbitrage.models.{
  Currency,
  CurrencyAmount,
  CurrencyCycle,
  CurrencyPair
}
import com.hootionary.scalarbitrage.services.PriceService
import scalax.collection.edge.WDiEdge
import scalax.collection.GraphPredef._
import scalax.collection.edge.Implicits._
import scalax.collection.immutable.Graph

import scala.collection.mutable

/*
  Implementation of the Bellman-Ford algorithm to find arbitrage opportunities.

  The total complexity of the algorithm is O(vertices * edges) due to the repeated relaxation of
  every edge for the amount of vertices that exist in the graph.
 */
object Arbitrage {

  def main(args: Array[String]): Unit = {

    val currencyPriceMap = PriceService.getPrices()

    val graph = currencyPriceMap.values
      .filter(v => v.finance != v.settle)
      .foldLeft(Graph[Currency, WDiEdge]()) { (g, v) =>
        g + v.finance ~> v.settle % -Math.log(v.price)
      }

    println("Graph: " + graph)

    val startNode = graph.nodes.head

    println("Start node: " + startNode)

    val distances = graph.nodes.foldLeft(mutable.Map[graph.NodeT, Double]()) {
      (m, v) =>
        m + (v -> Double.MaxValue)
    }

    distances(startNode) = 0.0

    val predecessors =
      graph.nodes.foldLeft(mutable.Map[graph.NodeT, Option[graph.NodeT]]()) {
        (m, v) =>
          m + (v -> Option.empty)
      }

    // Relax distances to each vertex
    // Total complexity: O(vertices * edges)

    // This loop runs in O(vertices)
    for (x <- 1 until graph.nodes.size) {

      // This loop runs in O(edges)
      for (e <- graph.edges) {

        val distance = distances(e.from) + e.weight
        if (distances(e.to) > distance) {
          distances(e.to) = distance
          predecessors(e.to) = Option(e.from)
        }
      }
    }

    println("Distances:")
    pprint.pprintln(distances)
    println("Predecessors:")
    pprint.pprintln(predecessors)

    // Negative cycle detection
    // Total complexity: O(edges)
    val cycles = graph.edges
      .filter(e => distances(e.from) + e.weight < distances(e.to))
      .map { e =>
        println(
          s"Negative cycle detected:\n"
            + s"    distances(${e.from}): ${distances(e.from)}\n"
            + s"    distances(${e.to}): ${distances(e.to)}\n"
            + s"    Edge: ${e}")

        val path = mutable.MutableList[Option[graph.NodeT]]()
        var predecessor = Option(e.to)

        while (!(path contains predecessor)) {
          path += predecessor
          predecessor = predecessor match {
            case Some(pred) => predecessors(pred)
            case None       => None
          }
        }

        val cycle = (path.reverse ++ Some(path.last))
          .filter(x => x.isDefined)
          .map(x => x.get)
          .toList

        println(s"    Path: ${cycle.mkString(" -> ")}")

        val pairs = cycle
          .sliding(2, 1)
          .map { l =>
            CurrencyPair(l.head.value, l(1).value)
          }
          .toList

        val conversions =
          pairs.scanLeft(CurrencyAmount(pairs.head.base, 1.0)) { (ca, pair) =>
            CurrencyAmount(pair.counter,
                           ca.amount * currencyPriceMap(pair).price)
          }
        println(s"    Conversions: ${conversions.mkString(" -> ")}")

        println()

        CurrencyCycle(cycle.map(x => x.value), conversions.last.amount)
      }

    val sortedCycles = cycles.toList.sortWith(_.multiplier > _.multiplier)
    val cycleStrings = sortedCycles.map(cycle =>
      f"Multiplier: ${cycle.multiplier}%2.5f, Cycle: ${cycle.path.map(c => c.symbol).mkString(" -> ")}")

    println(s"Arbitrage Opportunities:\n    ${cycleStrings.mkString("\n    ")}")
    println()
  }

}
