package com.hootionary.scalarbitrage

import com.hootionary.scalarbitrage.models.Types.CurrencyPriceMap
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
object Arbitrage extends App {

  override def main(args: Array[String]): Unit = {

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

        // Could use a Set to save a linear traversal
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

        val pairs = getPairs(cycle.map(c => c.value))

        val conversions = getConversions(pairs, currencyPriceMap)
        println(s"    Conversions: ${conversions.mkString(" -> ")}")

        println()

        CurrencyCycle(cycle.map(x => x.value), conversions.last.amount)
      }

    val sortedCycles = cycles.toList.sortWith(_.multiplier > _.multiplier)
    val cycleStrings = sortedCycles.map(cycle =>
      f"Multiplier: ${cycle.multiplier}%2.5f, Cycle: ${cycle.path.map(c => c.symbol).mkString(" -> ")}")

    println(
      s"Bellman-Ford Arbitrage Opportunities:\n    ${cycleStrings.mkString("\n    ")}")
    println()

    bruteForce(currencyPriceMap)
  }

  /** Calculates the solution with brute force */
  def bruteForce(currencyPriceMap: CurrencyPriceMap): Unit = {
    val currencies =
      currencyPriceMap.keySet.flatMap(cp => List(cp.base, cp.counter))

    val currencyCycles = getCycles(currencies)
      .map { path =>
        val conversions = getConversions(getPairs(path), currencyPriceMap)
        CurrencyCycle(path, conversions.last.amount)
      }
      .sortWith(_.multiplier > _.multiplier)

    val cycleStrings = currencyCycles.map(cycle =>
      f"Multiplier: ${cycle.multiplier}%2.5f, Cycle: ${cycle.path.map(c => c.symbol).mkString(" -> ")}")

    println(
      s"Brute-Force Arbitrage Opportunities:\n    ${cycleStrings.slice(0, 4).mkString("\n    ")}")

    println()
  }

  /** Gets all cycles given a set of currencies */
  def getCycles(currencies: Set[Currency]): List[List[Currency]] = {
    currencies.flatMap { currency =>
      val others = (currencies - currency).toList
      (1 to others.size)
        .flatMap(len =>
          others.combinations(len).toList.flatMap(x => x.permutations))
        .toSet
        .map((path: List[Currency]) => (currency :: path) :+ currency)
    }.toList
  }

  /** Gets a list of adjacent pairs with a sliding window */
  def getPairs(path: List[Currency]): List[CurrencyPair] = {
    path
      .sliding(2, 1)
      .map { l =>
        CurrencyPair(l.head, l(1))
      }
      .toList
  }

  /** Converts currencies through path and retains the intermediate solutions */
  def getConversions(
      pairs: List[CurrencyPair],
      currencyPriceMap: CurrencyPriceMap): List[CurrencyAmount] = {

    pairs.scanLeft(CurrencyAmount(pairs.head.base, 1.0)) { (ca, pair) =>
      CurrencyAmount(pair.counter, ca.amount * currencyPriceMap(pair).price)
    }
  }
}
