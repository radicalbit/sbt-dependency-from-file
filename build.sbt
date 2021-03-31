import sbt._

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
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
      developers := List(
          Developer(id = "francescofrontera",
              name = "Francesco Frontera",
              email = "francesco.frontera@radicalbit.io",
              url = url("https://github.com/francescofrontera")),
          Developer(id = "maocorte",
              name = "Mauro Cortellazzi",
              email = "mauro.cortellazzi@radicalbit.io",
              url = url("https://github.com/maocorte"))
      ),
    sbtPlugin := true
  )
  .settings(PublishSettings.settings: _*)
