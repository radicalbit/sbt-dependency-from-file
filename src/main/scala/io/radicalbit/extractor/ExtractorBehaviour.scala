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

import cats.data.Kleisli

import io.radicalbit.errors.ReducingResolverException
import io.radicalbit.models.Dependency
import sbt.{ MavenRepository, ModuleID, Credentials => SbtCredentials }
import cats.effect._
import cats.implicits._

trait ExtractorBehaviour[F[_]] {
  def resolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]]

  def credentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]]

  def modulesID: Kleisli[F, Seq[Dependency], Seq[ModuleID]]
}

object ExtractorBehaviour {
  def apply[F[_]: Effect]: ExtractorBehaviour[F] = behaviour[F]

  implicit def behaviour[F[_]: Effect]: ExtractorBehaviour[F] = new ExtractorBehaviour[F] {
    def resolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]] =
      Kleisli(dependencies =>
        dependencies
          .groupBy(_.resolver.url)
          .map { case (url, dependencies) =>
            dependencies.toSet.headOption
              .fold(ReducingResolverException(s"No Resolver was found in json for this resolver url $url").throwEx) {
                dependencySet =>
                  MavenRepository(dependencySet.resolver.name, url)
              }
          }
          .toSeq
          .pure[F]
      )

    def credentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]] =
      Kleisli(dependencies =>
        dependencies
          .flatMap(_.resolver.credentials)
          .distinct
          .map { c =>
            SbtCredentials(realm = c.realm, host = c.host, userName = c.user, passwd = c.password)
          }
          .pure[F]
      )

    def modulesID: Kleisli[F, Seq[Dependency], Seq[ModuleID]] = Kleisli(_.toModuleId.pure[F])
  }
}
