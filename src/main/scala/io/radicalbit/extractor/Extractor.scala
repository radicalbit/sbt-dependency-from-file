package io.radicalbit.extractor

import java.io.File

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import io.radicalbit.errors.{InvalidFieldException, ReducingResolverException}
import io.radicalbit.models._
import play.api.libs.json.Json
import sbt.{Credentials, MavenRepository}

trait Extractor[F[_]] {
  def load(file: File): Resource[F, Seq[Dependency]]

  def extractedResolvers: Kleisli[F, Seq[Dependency], Seq[MavenRepository]]

  def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[Credentials]]

  def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[sbt.ModuleID]]
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

    def extractedCredentials: Kleisli[F, Seq[Dependency], Seq[Credentials]] =
      Kleisli(
        dependencies =>
          dependencies
            .flatMap(_.resolver.credentials)
            .distinct
            .map { c =>
              Credentials(realm = c.realm,
                          host = c.host,
                          userName = c.user,
                          passwd = c.password)
            }
            .pure[F]
      )

    def extractedModuleId: Kleisli[F, Seq[Dependency], Seq[sbt.ModuleID]] =
      Kleisli(d => d.toModuleId.pure[F])
  }

}
