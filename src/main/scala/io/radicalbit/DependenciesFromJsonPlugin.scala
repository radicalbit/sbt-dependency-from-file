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

import cats.effect.IO
import io.radicalbit.extractor.Extractor
import io.radicalbit.models.DependenciesStructures
import sbt._

sealed trait KeysSetting {
  lazy val dependenciesJsonPath = settingKey[File]("Dependencies file path")
  lazy val dependenciesFromJson =
    settingKey[DependenciesStructures]("Extracted information")
}

object DependenciesFromJsonPlugin extends AutoPlugin {
  implicit val extractor: Extractor[IO] = Extractor.dependenciesExtractor

  object autoImports extends KeysSetting
  import autoImports._

  override def trigger: PluginTrigger = noTrigger

  override def requires = sbt.plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(dependenciesFromJson := {
      Extractor[IO]
        .load(dependenciesJsonPath.value)
        .use(extractDepResAndCred.run)
        .unsafeRunSync()
    })

  private[this] def extractDepResAndCred =
    for {
      d <- Extractor[IO].extractedModuleId
      r <- Extractor[IO].extractedResolvers
      c <- Extractor[IO].extractedCredentials
    } yield DependenciesStructures(d, r, c)
}
