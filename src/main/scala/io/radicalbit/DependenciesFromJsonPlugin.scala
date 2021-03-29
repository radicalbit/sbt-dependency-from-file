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

import cats.data.Kleisli

import java.io.File
import cats.effect._
import io.radicalbit.extractor.{ DependencyLoader, ExtractorBehaviour }
import io.radicalbit.models.{ DependenciesStructures, Dependency }
import sbt._
import sbt.plugins.JvmPlugin

sealed trait KeysSetting {
  lazy val dependenciesJsonPath = settingKey[File]("Dependencies file path")
  lazy val dependenciesFromJson =
    settingKey[DependenciesStructures]("Extracted information")
}

object DependenciesFromJsonPlugin extends AutoPlugin {
  object autoImports extends KeysSetting
  import autoImports._

  override def trigger: PluginTrigger = noTrigger

  override def requires: JvmPlugin.type = sbt.plugins.JvmPlugin

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      dependenciesFromJson := loadAndRun[IO](dependenciesJsonPath.value)
        .unsafeRunSync()
    )

  private[this] def loadAndRun[F[_]: Sync](file: File): F[DependenciesStructures] = {
    val behaviour =
      for {
        d <- ExtractorBehaviour[F].extractedModuleId
        r <- ExtractorBehaviour[F].extractedResolvers
        c <- ExtractorBehaviour[F].extractedCredentials
      } yield DependenciesStructures(d, r, c)

    DependencyLoader[F].load(file).use(behaviour.run)
  }
}
