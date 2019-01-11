package io.radicalbit

import java.io.File

import play.api.libs.json.Json
import sbt._

sealed trait ExtractDependenciesOps {
  import models._

  def extractDependenciesFromJson(dependenciesFile: File) = {
    val jsonAsString =
      scala.io.Source
        .fromFile(dependenciesFile)
        .getLines()
        .mkString("")

    Json
      .parse(jsonAsString)
      .as[Seq[Dependency]]
  }

  def extractedResolvers(dependenciesFile: File) = {
    val dependencies = this.extractDependenciesFromJson(dependenciesFile)

    dependencies
      .groupBy(_.resolver.name)
      .map {
        case (name, urls) =>
          val toSet: Option[Dependency] = urls.toSet.headOption
          toSet.fold(
            throw new IllegalArgumentException(
              s"No Resolver was found in json for this resolver name $name")) {
            dependencySet =>
              MavenRepository(name, dependencySet.resolver.url)
          }

      }
      .toSeq
  }
}

object ExtractDependenciesFromJsonPlugin
    extends AutoPlugin
    with ExtractDependenciesOps {

  object autoImport {
    lazy val dependenciesJsonPath = settingKey[File]("Dependencies file path")
    lazy val extractedDependencies =
      settingKey[Seq[ModuleID]]("Extracted dependencies")
    lazy val extractedResolver =
      settingKey[Seq[sbt.MavenRepository]]("Extract MavenResolver")
  }

  import autoImport._

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    extractedDependencies := extractDependenciesFromJson(
      dependenciesJsonPath.value).toModuleId,
    extractedResolver := extractedResolvers(dependenciesJsonPath.value)
  )
}
