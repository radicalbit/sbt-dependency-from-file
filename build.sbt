import sbt._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.8",
    name := "sbt-dependency-from-file",
    organization := "io.radicalbit",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Dependencies.pluginDependencies,
    sbtPlugin := true
  )
  .settings(PublishSettings.settings: _*)
