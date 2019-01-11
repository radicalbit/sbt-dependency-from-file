package io.radicalbit.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

sealed case class Resolver(name: String, url: String)

object Resolver {
  implicit lazy val resolverReader: Reads[Resolver] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "url").read[String]
  )(Resolver.apply _)
}
