package io.radicalbit

import java.io.File

import io.radicalbit.errors.{InvalidFieldException, ReducingCredentialsException, ReducingResolverException}
import io.radicalbit.models.Dependency
import play.api.libs.json.{JsPath, Json, JsonValidationError}
import sbt._

import scala.util.Try

sealed trait ExtractDependenciesOps {
  private def readJsonFile(file: File) = Try {
    scala.io.Source
      .fromFile(file)
      .getLines()
      .mkString("")
  }

  private def parseAndValidateJson(jsonString: String) =
    Json
      .parse(jsonString)
      .validate[Seq[Dependency]]
      .fold(errors => InvalidFieldException(errors.mkString(", ")).throwEx,
            identity)

  def extractDependenciesTask(dependenciesFile: File): Seq[Dependency] =
    this
      .readJsonFile(dependenciesFile)
      .map(parseAndValidateJson)
      .get

  def extractedResolversTask(dependenciesFile: File): Seq[MavenRepository] = {
    val dependencies = this.extractDependenciesTask(dependenciesFile)

    dependencies
      .groupBy(_.resolver.url)
      .map {
        case (url, dependencies) =>
          dependencies.toSet.headOption
            .fold(ReducingResolverException(s"No Resolver was found in json for this resolver url $url").throwEx) { dependencySet =>
                MavenRepository(dependencySet.resolver.name, url)
            }
      }.toSeq
  }

  def extractedCredentialsTask(dependenciesFile: File): Seq[Credentials] =
    this.extractDependenciesTask(dependenciesFile)
      .flatMap(_.resolver.credentials)
      .distinct
      .map { credential =>
          Credentials(realm = credential.realm, host = credential.host, userName = credential.user, passwd = credential.password)
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
    lazy val extractedCredentials =
      settingKey[Seq[sbt.Credentials]]("Extracted Credentials")
  }

  import autoImport._

  override def trigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    extractedDependencies := extractDependenciesTask(dependenciesJsonPath.value).toModuleId,
    extractedResolvers := extractedResolversTask(dependenciesJsonPath.value),
    extractedCredentials := extractedCredentialsTask(dependenciesJsonPath.value)
  )
}
