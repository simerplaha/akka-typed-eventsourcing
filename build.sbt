import sbt.Keys._

name := "akka-typed-eventsourcing"

version := "1.0"

scalaVersion := "2.11.7"

val akkaV = "2.4.0"
val sprayV = "1.3.3"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "io.spray" %% "spray-client" % sprayV,
  "io.spray" %% "spray-httpx" % sprayV,
  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.akka" %% "akka-typed-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "3.0.0-M10" % "test",
  "org.specs2" %% "specs2-common" % "2.3.11" % "test",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
  "commons-net" % "commons-net" % "3.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalaj" %% "scalaj-http" % "1.1.5",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.0-1",
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.0",
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "com.github.tminglei" %% "slick-pg" % "0.10.1",
  "com.zaxxer" % "HikariCP" % "2.4.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.google.code.gson" % "gson" % "2.4",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "net.liftweb" %% "lift-json" % "3.0-M5-1",
  "net.liftweb" %% "lift-json-ext" % "3.0-M5-1"
)