package io.radicalbit.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

sealed case class Resolver(name: String, url: String, credentials: Option[Credentials])

object Resolver {
  private lazy val regexForUrl =
    """((([A-Za-z]{3,9}:(?:\/\/)?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-]+|(?:www\.|[\-;:&=\+\$,\w]+@)[A-Za-z0-9\.\-]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w]*))?)""".r

  implicit lazy val resolverReader: Reads[Resolver] = (
    (JsPath \ "name").read(minLength[String](1)) ~
      (JsPath \ "url").read(pattern(regexForUrl, "Malformed resolver URL")) ~
      (JsPath \ "credentials").readNullable[Credentials]
  )(Resolver.apply _)
}
