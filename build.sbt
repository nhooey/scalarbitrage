name := "scalarbitrage"

version := "0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "org.scala-graph" %% "graph-core" % "1.12.5"
)

mainClass in(Compile, run) := Some("com.hootionary.scalarbitrage.Main")
