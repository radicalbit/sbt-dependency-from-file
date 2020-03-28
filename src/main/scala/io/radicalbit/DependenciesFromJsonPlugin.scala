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

trait MinimalSetting extends AutoPlugin {
  override def trigger: PluginTrigger = noTrigger
  override def requires = sbt.plugins.JvmPlugin

  lazy val dependenciesJsonPath = settingKey[File]("Dependencies file path")
  lazy val dependenciesFromJson =
    settingKey[DependenciesStructures]("Extracted information")
}

object DependenciesFromJsonPlugin extends MinimalSetting {
  implicit val extractor: Extractor[IO] = Extractor.dependenciesExtractor

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    dependenciesFromJson := {
      Extractor[IO]
        .load(dependenciesJsonPath.value)
        .use { dependencies =>
          (for {
            d <- extractor.extractedModuleId
            r <- extractor.extractedResolvers
            c <- extractor.extractedCredentials
          } yield DependenciesStructures(d, r, c)).run(dependencies)
        }
        .unsafeRunSync()
    }
  )
}
