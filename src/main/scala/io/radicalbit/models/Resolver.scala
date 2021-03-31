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
