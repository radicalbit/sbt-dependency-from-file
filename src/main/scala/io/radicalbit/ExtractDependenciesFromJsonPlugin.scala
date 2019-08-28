/*
 * Copyright 2019 Radicalbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
