package io.radicalbit.models

sealed case class Resolver(name: String, url: String)

object Resolver {
  import play.api.libs.json._ // JSON library
  import play.api.libs.json.Reads._ // Custom validation helpers
  import play.api.libs.functional.syntax._ // Combinator syntax

  implicit lazy val resolverReader: Reads[Resolver] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "url").read[String]
  )(Resolver.apply _)

  implicit lazy val resolverWriter: Writes[Resolver] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "url").write[String]
  )(unlift(Resolver.unapply))

  implicit lazy val resolverFormat: Format[Resolver] = Format(resolverReader, resolverWriter)
}
