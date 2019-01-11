package io.radicalbit

import java.io.File

import play.api.libs.json.Json
import sbt._

import scala.util.Try

sealed trait ExtractDependenciesOps {
  import models._

  private def readJsonFile(file: File) = Try {
    scala.io.Source
      .fromFile(file)
      .getLines()
      .mkString("")
  }

  def extractDependenciesTask(dependenciesFile: File): Seq[Dependency] =
    this
      .readJsonFile(dependenciesFile)
      .map(Json.parse(_).as[Seq[Dependency]])
      .getOrElse(throw new RuntimeException(
        s"Error during file reading ${dependenciesFile.getPath}"))

  def extractedResolversTask(dependenciesFile: File): Seq[MavenRepository] = {
    val dependencies = this.extractDependenciesTask(dependenciesFile)

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
    lazy val extractedResolvers =
      settingKey[Seq[sbt.MavenRepository]]("Extracted MavenResolver")
  }

  import autoImport._

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    extractedDependencies := extractDependenciesTask(dependenciesJsonPath.value).toModuleId,
    extractedResolvers := extractedResolversTask(dependenciesJsonPath.value)
  )
}
