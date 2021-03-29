package io.radicalbit.extractor

import cats.data.Kleisli
import cats.effect.Sync
import io.radicalbit.errors.ReducingResolverException
import io.radicalbit.models.Dependency
import sbt.{ MavenRepository, ModuleID, Credentials => SbtCredentials }
import cats.effect._
import cats.implicits._

trait ExtractorBehaviour[F[_]] {
  def extractedResolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]]

  def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]]

  def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[ModuleID]]
}

object ExtractorBehaviour {
  def apply[F[_]: Sync]: ExtractorBehaviour[F] = makeInst[F]

  implicit def makeInst[F[_]: Sync]: ExtractorBehaviour[F] = new ExtractorBehaviour[F] {
    def extractedResolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]] =
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

    def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[SbtCredentials]] =
      Kleisli(dependencies =>
        dependencies
          .flatMap(_.resolver.credentials)
          .distinct
          .map { c =>
            SbtCredentials(realm = c.realm, host = c.host, userName = c.user, passwd = c.password)
          }
          .pure[F]
      )

    def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[ModuleID]] = Kleisli(_.toModuleId.pure[F])
  }
}
