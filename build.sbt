name := "scalarbitrage"

version := "0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "pprint" % "0.5.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "org.scala-graph" %% "graph-core" % "1.12.5"
)

mainClass in(Compile, run) := Some("com.hootionary.scalarbitrage.Arbitrage")
