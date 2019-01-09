package io.radicalbit

import java.io.File

import play.api.libs.json.Json
import sbt._

object ExtractDependenciesFromJsonPlugin extends AutoPlugin {
  import models._

  object autoImport {
    lazy val dependenciesJsonPath = SettingKey[File]("dependenciesJsonPath")
    lazy val extractedDependencies = SettingKey[Seq[ModuleID]]("extractedDependencies")
    lazy val extractedResolver = SettingKey[Seq[MavenRepository]]("extractedResolver")
  }

  import autoImport._

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  private def extracDependenciesFromJson(dependenciesFile: File) = {
    val jsonAsString =
      scala.io.Source
        .fromFile(dependenciesFile)
        .getLines()
        .mkString("")

    Json
      .parse(jsonAsString)
      .as[Seq[Dependency]]
  }

  private def extractedResolvers(dependenciesFile: File) = {
    val dependencies = this.extracDependenciesFromJson(dependenciesFile)

    dependencies
      .groupBy(_.resolver.name)
      .map {
        case (name, url) => MavenRepository(name, url.head.resolver.url)
      }
      .toSeq
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    extractedDependencies := extracDependenciesFromJson(dependenciesJsonPath.value).toModuleId,
    extractedResolver := extractedResolvers(dependenciesJsonPath.value)
  )
}
