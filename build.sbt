import sbt._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.10.4",
    name := "parsing-json-dependency-plugin",
    organization := "io.radicalbit",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Dependencies.pluginDependencies,
    sbtPlugin := true
  )
