import mill._, scalalib._

object scalarbitrage extends ScalaModule {
  def scalaVersion = "2.12.4"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::pprint:0.5.5",
    ivy"com.typesafe.akka::akka-http-spray-json:10.1.8",
    ivy"org.scalaj::scalaj-http:2.4.1",
    ivy"org.scala-graph::graph-core:1.12.5"
  )
}
