import sbt._

licenses += ("Apache-2.0", new URL(
  "https://www.apache.org/licenses/LICENSE-2.0.txt"))

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.8",
    name := "sbt-dependency-from-file",
    organization := "io.radicalbit",
    description := "sbt-dependency-from-file is a fresh-made library for dynamic load sbt dependencies using json file.",
    organizationName := "Radicalbit",
    startYear := Some(2019),
    version := "1.0",
    libraryDependencies ++= Dependencies.pluginDependencies,
    publishMavenStyle := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None,
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
