package io.radicalbit.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import sbt._

sealed case class Dependency(groupId: String,
                             artifactId: String,
                             version: String,
                             scalaVersion: Option[String],
                             resolver: Resolver)

object Dependency {
  implicit lazy val readDependency: Reads[Dependency] = (
    (JsPath \ "groupId").read[String] and
      (JsPath \ "artifactId").read[String] and
      (JsPath \ "version").read[String] and
      (JsPath \ "scalaVersion").readNullable[String] and
      (JsPath \ "resolver").read[Resolver]
  )(Dependency.apply _)

  implicit class Conversion(ls: Seq[Dependency]) {
    def toModuleId: Seq[ModuleID] = ls.map {
      case Dependency(groupId, artifactId, version, Some(_), _) =>
        groupId %% artifactId % version
      case Dependency(groupId, artifactId, version, _, _) =>
        groupId % artifactId % version
    }
  }
}
