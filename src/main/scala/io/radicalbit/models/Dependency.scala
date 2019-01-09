package io.radicalbit.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import sbt._

sealed case class Dependency(groupId: String,
                             artifactId: String,
                             version: String,
                             resolver: Resolver)

object Dependency {
  implicit lazy val readDependency: Reads[Dependency] = (
    (JsPath \ "groupId").read[String] and
      (JsPath \ "artifactId").read[String] and
      (JsPath \ "version").read[String] and
      (JsPath \ "resolver").read[Resolver]
  )(Dependency.apply _)

  implicit lazy val writeDependency: Writes[Dependency] = (
    (JsPath \ "groupId").write[String] and
      (JsPath \ "artifactId").write[String] and
      (JsPath \ "version").write[String] and
      (JsPath \ "resolver").write[Resolver]
  )(unlift(Dependency.unapply))

  implicit val dependencyFormat: Format[Dependency] = Format(readDependency, writeDependency)

  implicit class Conversion(ls: Seq[Dependency]) {
    @inline def toModuleId: Seq[ModuleID] = ls.map(dep => dep.groupId %% dep.artifactId % dep.version)
  }
}
