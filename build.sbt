import sbt._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.10.4",
    name := "sbt-dependency-from-file",
    organization := "io.radicalbit",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Dependencies.pluginDependencies,
    sbtPlugin := true
  )
  .settings(PublishSettings.settings: _*)
