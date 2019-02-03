package io.radicalbit.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

sealed case class Credentials(realm: String, host: String, user: String, password: String)

object Credentials {

  implicit lazy val plainCredentialsReader: Reads[Credentials] = (
    (JsPath \ "realm").read(minLength[String](1)) ~
      (JsPath \ "host").read(minLength[String](1)) ~
      (JsPath \ "user").read(minLength[String](1)) ~
      (JsPath \ "password").read(minLength[String](1))
    )(Credentials.apply _)
}
