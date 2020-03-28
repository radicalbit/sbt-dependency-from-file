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

package io.radicalbit.extractor

import java.io.File

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import io.radicalbit.errors._
import io.radicalbit.models._
import play.api.libs.json.Json
import sbt.{ModuleID, MavenRepository, Credentials => SbtCredentials}

sealed trait Extractor[F[_]] {
  def load(file: File): Resource[F, Seq[Dependency]]

  def extractedResolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]]

  def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]]

  def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[ModuleID]]
}

object Extractor {
  def apply[F[_]](implicit extractor: Extractor[F]): extractor.type = extractor

  def dependenciesExtractor[F[_]: Sync] = new DependenciesExtractor[F]

  class DependenciesExtractor[F[_]: Sync] extends Extractor[F] {
    def load(file: File): Resource[F, Seq[Dependency]] =
      Resource
        .fromAutoCloseable(Sync[F].delay(scala.io.Source.fromFile(file)))
        .evalMap { buffer =>
          Sync[F].fromEither {
            Json
              .parse(buffer.getLines().mkString(""))
              .validate[Seq[Dependency]]
              .asEither
              .leftMap(errors => InvalidFieldException(errors.mkString(", ")))
          }
        }

    def extractedResolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]] =
      Kleisli(
        dependencies =>
          dependencies
            .groupBy(_.resolver.url)
            .map {
              case (url, dependencies) =>
                dependencies.toSet.headOption
                  .fold(ReducingResolverException(
                    s"No Resolver was found in json for this resolver url $url").throwEx) {
                    dependencySet =>
                      MavenRepository(dependencySet.resolver.name, url)
                  }
            }
            .toSeq
            .pure[F]
      )

    def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]] =
      Kleisli(
        dependencies =>
          dependencies
            .flatMap(_.resolver.credentials)
            .distinct
            .map { c =>
              SbtCredentials(realm = c.realm,
                             host = c.host,
                             userName = c.user,
                             passwd = c.password)
            }
            .pure[F]
      )

    def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[ModuleID]] =
      Kleisli(_.toModuleId.pure[F])
  }

}
