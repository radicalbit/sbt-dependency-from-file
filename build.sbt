import sbt._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.8",
    name := "sbt-dependency-from-file",
    organization := "io.radicalbit",
    version := "1.5-SNAPSHOT",
    scalafmtOnCompile := true,
    organizationName := "Radicalbit",
    startYear := Some(2019),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    libraryDependencies ++= Dependencies.pluginDependencies,
    sbtPlugin := true
  )
  .settings(PublishSettings.settings: _*)
